/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */
package com.kinvey.android;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.google.common.base.Preconditions;
import com.kinvey.java.Logger;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialStore;

/**
 * @author mjsalinger
 * @since 2.0
 */
class AndroidCredentialStore implements CredentialStore {
    private static final String TAG = AndroidCredentialStore.class.getSimpleName();

    private HashMap<String, Credential> credentials;
    private Context appContext;

    AndroidCredentialStore(Context context) throws IOException, AndroidCredentialStoreException {
        appContext = context.getApplicationContext();
        credentials = new HashMap<String, Credential>();
        try {
            retrieveCredentialStore();
        } catch (ClassNotFoundException ex) {
            credentials = new HashMap<String, Credential>();
            persistCredentialStore();
            throw new AndroidCredentialStoreException("Credential store corrupted and was rebuilt");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Credential load(String userId) throws IOException {
        Credential credential = credentials.get(userId);
        return credential;
    }

    /** {@inheritDoc} */
    @Override
    public void store(String userId, Credential credential) throws IOException {
        Preconditions.checkNotNull(credential, "credential must not be null");
        Preconditions.checkNotNull(userId, "userId must not be null");

        credentials.put(userId, credential);
        persistCredentialStore();
    }

    /** {@inheritDoc} */
    @Override
    public void delete(String userId) {
        credentials.remove(userId);
        persistCredentialStore();
    }

    private void persistCredentialStore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new PersistCredentialStore().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new PersistCredentialStore().execute();
        }
    }

    private void retrieveCredentialStore() throws ClassNotFoundException {
        FileInputStream fIn = null;
        ObjectInputStream in = null;

        if (appContext.getFileStreamPath("kinveyCredentials.bin").exists()) {
            try {
                fIn = appContext.openFileInput("kinveyCredentials.bin");
                in = new ObjectInputStream(fIn);
                credentials = (HashMap<String, Credential>) in.readObject();
            } catch (IOException ex) {
            	Logger.WARNING("Corrupt credential store detected");
            } finally {
                try {
                    if (fIn != null) {
                        fIn.close();
                    }

                    if (in != null) {
                        in.close();
                    }

                } catch (IOException ioe) {
                	Logger.WARNING("Could not clean up resources");
                }
            }
        } else {
            persistCredentialStore();
        }
    }

    private class PersistCredentialStore extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                FileOutputStream fStream = appContext.openFileOutput("kinveyCredentials.bin", Context.MODE_PRIVATE);
                ObjectOutputStream oStream = new ObjectOutputStream(fStream);

                oStream.writeObject(credentials);
                oStream.flush();
                fStream.getFD().sync();
                oStream.close();

                Logger.INFO("Serialization success");
            } catch (Exception e) {
            	Logger.ERROR("Error on persisting credential store");
            }
            return null;
        }
    }
}
