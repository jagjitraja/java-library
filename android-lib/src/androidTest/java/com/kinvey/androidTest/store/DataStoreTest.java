package com.kinvey.androidTest.store;


import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyPurgeCallback;
import com.kinvey.android.store.AsyncDataStore;
import com.kinvey.android.store.AsyncUserStore;
import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DataStoreTest {

    private Client client;
    private AsyncDataStore<Person> personAsyncDataStore;


    @Before
    public void setUp() throws InterruptedException, IOException {
        Context mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        client = new Client.Builder(mMockContext).build();
        final CountDownLatch latch = new CountDownLatch(1);
        if (!client.isUserLoggedIn()) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    try {
                        AsyncUserStore.login("test", "test", client, new KinveyClientCallback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                latch.countDown();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Looper.loop();
                }
            }).start();
        } else {
            latch.countDown();
        }
        latch.await();
    }


    private static class DefaultKinveyClientCallback implements KinveyClientCallback<Person> {

        private CountDownLatch latch;
        Person result;
        Throwable error;

        DefaultKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Person result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyPurgeCallback implements KinveyPurgeCallback {

        private CountDownLatch latch;
        Throwable error;

        DefaultKinveyPurgeCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Void result) {
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveySyncCallback implements KinveySyncCallback<Person> {

        private CountDownLatch latch;
        KinveyPushResponse kinveyPushResponse;
        KinveyPullResponse<Person> kinveyPullResponse;
        Throwable error;

        DefaultKinveySyncCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPushResponse kinveyPushResponse, KinveyPullResponse<Person> kinveyPullResponse) {
            this.kinveyPushResponse = kinveyPushResponse;
            this.kinveyPullResponse = kinveyPullResponse;
            finish();
        }

        @Override
        public void onPullStarted() {

        }

        @Override
        public void onPushStarted() {

        }

        @Override
        public void onPullSuccess(KinveyPullResponse<Person> kinveyPullResponse) {
            this.kinveyPullResponse = kinveyPullResponse;
        }

        @Override
        public void onPushSuccess(KinveyPushResponse kinveyPushResponse) {
            this.kinveyPushResponse = kinveyPushResponse;
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyPushCallback implements KinveyPushCallback {

        private CountDownLatch latch;
        KinveyPushResponse result;
        Throwable error;

        DefaultKinveyPushCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPushResponse result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        @Override
        public void onProgress(long current, long all) {

        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyPullCallback implements KinveyPullCallback<Person> {

        private CountDownLatch latch;
        KinveyPullResponse<Person> result;
        Throwable error;

        DefaultKinveyPullCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(KinveyPullResponse<Person> result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyDeleteCallback implements KinveyDeleteCallback {

        private CountDownLatch latch;
        Integer result;
        Throwable error;

        DefaultKinveyDeleteCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Integer result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultKinveyListCallback implements KinveyListCallback<Person> {

        private CountDownLatch latch;
        List<Person> result;
        Throwable error;

        DefaultKinveyListCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(List<Person> result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private Person createPerson(String name) {
        Person person = new Person();
        person.setUsername(name);
        return person;
    }


    private DefaultKinveyClientCallback save(final AsyncDataStore<Person> store, final Person person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.save(person, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }


    @Test
    public void testSave() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        String personName = "TestName";
        DefaultKinveyClientCallback callback = save(store, createPerson(personName));
        assertNotNull(callback.result);
        assertNotNull(callback.result.getUsername());
        assertNull(callback.error);
        assertTrue(callback.result.getUsername().equals(personName));
    }


    @Test
    public void testCustomTag() {
        String path = client.getContext().getFilesDir().getAbsolutePath();
        String customPath = path + "/_baas.kinvey.com_-1";
        removeFiles(customPath);
        File file = new File(customPath);
        assertFalse(file.exists());
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        assertTrue(file.exists());
    }

    private void removeFiles(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        String lockPath = path.concat(".lock");
        file = new File(lockPath);
        if (file.exists()) {
            file.delete();
        }
        String logPath = path.concat(".management");
        file = new File(logPath);
        if (file.exists()) {
            file.delete();
        }
    }


    private DefaultKinveyPurgeCallback purge(final AsyncDataStore<Person> store) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPurgeCallback callback = new DefaultKinveyPurgeCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.purge(callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    @Test
    public void testPurge() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNull(purgeCallback.error);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    @Test
    public void testPurgeInvalidDataStoreType() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.NETWORK);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNotNull(purgeCallback.error);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    @Test
    public void testPurgeTimeoutError() throws InterruptedException, IOException {
        final ChangeTimeout changeTimeout = new ChangeTimeout();
        HttpRequestInitializer initializer = new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws SocketTimeoutException {
                changeTimeout.initialize(request);
            }
        };

        client = new Client.Builder(client.getContext())
                .setHttpRequestInitializer(initializer)
                .build();

        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);

        Person person = createPerson("PersonForTestPurge");
        save(store, person);
        person.setAge("25");
        save(store, person);

        DefaultKinveyPurgeCallback purgeCallback = purge(store);
        assertNotNull(purgeCallback.error);
        assertNotNull(purgeCallback.error.getCause());
        assertTrue(purgeCallback.error.getCause().getClass() == SocketTimeoutException.class);
    }


    private DefaultKinveySyncCallback sync(final AsyncDataStore<Person> store, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveySyncCallback callback = new DefaultKinveySyncCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.sync(callback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testSync() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNull(syncCallback.error);
        assertNotNull(syncCallback.kinveyPushResponse);
        assertNotNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testSyncInvalidDataStoreType() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.NETWORK);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPurge"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNotNull(syncCallback.error);
        assertNull(syncCallback.kinveyPushResponse);
        assertNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    @Test
    public void testSyncTimeoutError() throws InterruptedException {
        final ChangeTimeout changeTimeout = new ChangeTimeout();
        HttpRequestInitializer initializer = new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws SocketTimeoutException {
                changeTimeout.initialize(request);
            }
        };
        client = new Client.Builder(client.getContext())
                .setHttpRequestInitializer(initializer)
                .build();
        client.getSycManager().clear(Person.COLLECTION);
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        Person person = createPerson("PersonForTestSyncTimeOut");
        save(store, person);
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNotNull(syncCallback.error);
        assertTrue(syncCallback.error.getClass() == SocketTimeoutException.class);
    }


    @Test
    public void testSyncNoCompletionHandler() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForSyncNoCompletionHandler"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveySyncCallback syncCallback = sync(store, 60);
        assertFalse(syncCallback.error == null && syncCallback.kinveyPullResponse == null && syncCallback.kinveyPushResponse == null);
        assertNotNull(syncCallback.kinveyPushResponse);
        assertNotNull(syncCallback.kinveyPullResponse);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }

    private DefaultKinveyPushCallback push(final AsyncDataStore<Person> store, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPushCallback callback = new DefaultKinveyPushCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.push(callback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testPush() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPush"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    @Test
    public void testPushInvalidDataStoreType() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.NETWORK);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForTestPush"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNotNull(pushCallback.error);
        assertNull(pushCallback.result);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }

    @Test
    public void testPushNoCompletionHandler() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);
        save(store, createPerson("PersonForPushNoCompletionHandler"));
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 1);
        DefaultKinveyPushCallback pushCallback = push(store, 60);
        assertFalse(pushCallback.error == null && pushCallback.result == null);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);
        assertTrue(client.getSycManager().getCount(Person.COLLECTION) == 0);
    }


    private DefaultKinveyPullCallback pull(final AsyncDataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyPullCallback callback = new DefaultKinveyPullCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                if (query != null) {
                    store.pull(query, callback);
                } else {
                    store.pull(callback);
                }
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    private DefaultKinveyDeleteCallback delete(final AsyncDataStore<Person> store, final Query query) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.delete(query, callback);
                Looper.loop();
            }
        }).start();
        latch.await();
        return callback;
    }

    //cleaning backend store
    private void cleanBackendDataStore(AsyncDataStore<Person> store) throws InterruptedException {
        DefaultKinveySyncCallback syncCallback = sync(store, 120);
        assertNull(syncCallback.error);
        Query query = client.query();
        query = query.notEqual("age", "100500");
        DefaultKinveyDeleteCallback deleteCallback = delete(store, query);
        assertNull(deleteCallback.error);
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        Log.d("testPull", " : clearing backend store successful");
    }

    //use for Person.COLLECTION and for Person.class
    private int getCacheSize(StoreType storeType) {
        return client.getCacheManager().getCache(Person.COLLECTION, Person.class, storeType.ttl).get().size();
    }

    //TODO check: getCacheSize(StoreType.SYNC) return 0 after saving items to store cache
    @Test
    public void testPull() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        cleanBackendDataStore(store);

        // uploading 3 person to backend
        ArrayList<Person> persons = new ArrayList<>();
        Person victor = createPerson("Victor_" + UUID.randomUUID().toString());
        Person hugo = createPerson("Hugo_" + UUID.randomUUID().toString());
        Person barros = createPerson("Barros_" + UUID.randomUUID().toString());
        persons.add(victor);
        persons.add(hugo);
        persons.add(barros);
        for (Person person : persons) {
            save(store, person);
        }
        DefaultKinveyPushCallback pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        //test pulling all data from backend
        DefaultKinveyPullCallback pullCallback = pull(store, null);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getResult().size() == 3);
//        assertTrue(pullCallback.result.getResult().size() == getCacheSize(StoreType.SYNC));

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        //test pull only 1 item by query
        Query query = client.query();
        query = query.equals("username", victor.getUsername());
        pullCallback = pull(store, query);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getResult().size() == 1);
        assertTrue(pullCallback.result.getResult().get(0).getUsername().equals(victor.getUsername()));
//        assertTrue(pullCallback.result.getResult().size() == getCacheSize(StoreType.SYNC));

        cleanBackendDataStore(store);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        //creating 1 entity and uploading to backend
        save(store, hugo);
        pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();
        //test pulling not existing data from backend
        query = client.query();
        query = query.equals("username", victor.getUsername());
        pullCallback = pull(store, query);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getResult().size() == 0);
//        assertTrue(pullCallback.result.getResult().size() == getCacheSize(StoreType.SYNC));

        cleanBackendDataStore(store);
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();

        //creating 1 entity and uploading to backend
        save(store, victor);
        pushCallback = push(store, 120);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);

        //cleaning cache store
        client.getCacheManager().getCache(Person.COLLECTION, Person.class, StoreType.SYNC.ttl).clear();
        //test pulling 1 entity if only 1 entity exist at backend
        query = client.query();
        query = query.equals("username", victor.getUsername());
        pullCallback = pull(store, query);
        assertNull(pullCallback.error);
        assertNotNull(pullCallback.result);
        assertTrue(pullCallback.result.getResult().size() == 1);
        assertTrue(pullCallback.result.getResult().get(0).getUsername().equals(victor.getUsername()));
//        assertTrue(pullCallback.result.getResult().size() == getCacheSize(StoreType.SYNC));
    }


    //TODO check: can we do pull if unsaved data exists?
/*    @Test
    public void testPullPendingSyncItems() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        save(store, createPerson("TestPullPendingSyncItems"));

        DefaultKinveyPullCallback pullCallback = pull(store, null);
        assertNull(pullCallback.result);
        assertNotNull(pullCallback.error);
    }*/


    @Test
    public void testPullInvalidDataStoreType() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.NETWORK);
        client.getSycManager().clear(Person.COLLECTION);

        DefaultKinveyPullCallback pullCallback = pull(store, null);
        assertNull(pullCallback.result);
        assertNotNull(pullCallback.error);
    }


    private DefaultKinveyClientCallback find(final AsyncDataStore<Person> store, final String id, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyClientCallback callback = new DefaultKinveyClientCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.find(id, callback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testFindById() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        Person person = createPerson("TestFindByIdPerson");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        String personId = saveCallback.result.getId();

        DefaultKinveyClientCallback findCallback = find(store, personId, 60);
        assertNotNull(findCallback.result);
        assertNull(saveCallback.error);
        assertEquals(findCallback.result.getId(), personId);
    }


    private DefaultKinveyListCallback find(final AsyncDataStore<Person> store, final Query query, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyListCallback callback = new DefaultKinveyListCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.find(query, callback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testFindByQuery() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        Person person = createPerson("TestFindByIdPerson");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        String userId = saveCallback.result.getId();
        Query query = client.query();
        query = query.equals("_id", userId);

        DefaultKinveyListCallback kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertNotNull(kinveyListCallback.result.get(0));
        assertEquals(kinveyListCallback.result.get(0).getId(), userId);
    }


    private DefaultKinveyDeleteCallback delete(final AsyncDataStore<Person> store, final String id, int seconds) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.delete(id, callback);
                Looper.loop();
            }
        }).start();
        latch.await(seconds, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testRemovePersistable() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        Person person = createPerson("TestFindByIdPerson");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        String userId = saveCallback.result.getId();

        DefaultKinveyDeleteCallback deleteCallback = delete(store, userId, 60);
        assertNull(deleteCallback.error);
        assertNotNull(deleteCallback.result);
        assertTrue(deleteCallback.result == 1);
    }


    @Test
    public void testRemovePersistableIdMissing() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        Person person = createPerson("TestFindByIdPerson");
        DefaultKinveyClientCallback saveCallback = save(store, person);
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());

        DefaultKinveyDeleteCallback deleteCallback = delete(store, null, 60);
        assertNotNull(deleteCallback.error);
        assertNull(deleteCallback.result);
    }

    private DefaultKinveyDeleteCallback delete(final AsyncDataStore<Person> store, final Iterable<String> entityIDs) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultKinveyDeleteCallback callback = new DefaultKinveyDeleteCallback(latch);
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                store.delete(entityIDs, callback);
                Looper.loop();
            }
        }).start();
        latch.await(120, TimeUnit.SECONDS);
        return callback;
    }

    @Test
    public void testRemovePersistableArray() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        DefaultKinveyClientCallback saveCallback = save(store, createPerson("Test1FindByIdPerson"));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String user1Id = saveCallback.result.getId();

        saveCallback = save(store, createPerson("Test2FindByIdPerson"));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String user2Id = saveCallback.result.getId();

        assertNotEquals(user1Id, user2Id);

        DefaultKinveyDeleteCallback deleteCallback = delete(store, Arrays.asList(user1Id, user2Id));
        assertNull(deleteCallback.error);
        assertNotNull(deleteCallback.result);
        assertTrue(deleteCallback.result == 2);
    }


    //TODO check: need create request for deleting all items from store
/*    @Test
    public void testRemoveAll() throws InterruptedException {
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        DefaultKinveyClientCallback saveCallback = save(store, createPerson("TestRemoveAll1Person"));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String user1Id = saveCallback.result.getId();

        saveCallback = save(store, createPerson("TestRemoveAll2Person"));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        assertNotNull(saveCallback.result.getId());
        String user2Id = saveCallback.result.getId();

        assertNotEquals(user1Id, user2Id);

//        deleting all items

        DefaultKinveyDeleteCallback deleteCallback  = delete(store, query);
        assertNull(deleteCallback.error);
        assertNotNull(deleteCallback.result);
        assertTrue(deleteCallback.result == 2);
    }*/


    //TODO check: result.size() should be 0
    @Test
    public void testExpiredTTL() throws InterruptedException {
        StoreType.SYNC.ttl = 1;
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        client.getSycManager().clear(Person.COLLECTION);

        DefaultKinveyClientCallback saveCallback = save(store, createPerson("Test1FindByIdPerson"));
        assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        String userId = saveCallback.result.getId();
        assertNotNull(userId);

        Thread.sleep(1000);

        Query query = client.query();
        query = query.equals("_id", userId);

        DefaultKinveyListCallback findCallback = find(store, query, 120);
        assertNull(findCallback.error);
        assertNotNull(findCallback.result);
        assertTrue(findCallback.result.size() == 0);
        StoreType.SYNC.ttl = Long.MAX_VALUE;
    }


    @Test
    public void testSaveAndFind10SkipLimit() throws IOException, InterruptedException {
        assertNotNull(client.getUser());
        AsyncDataStore<Person> store = client.dataStore(Person.COLLECTION, Person.class, StoreType.SYNC);
        cleanBackendDataStore(store);
        sync(store, 120);

        User user = client.getUser();

        for (int i = 0; i < 10; i++) {
            Person person = createPerson("Person_" + i);
            DefaultKinveyClientCallback saveCallback = save(store, person);
            assertNull(saveCallback.error);
            assertNotNull(saveCallback.result);
        }

        int skip = 0;
        int limit = 2;

        DefaultKinveyListCallback kinveyListCallback;
        Query query = client.query();
        for (int i = 0; i < 5; i++) {
            query.setSkip(skip);
            query.setLimit(limit);
            kinveyListCallback = find(store, query, 60);
            assertNull(kinveyListCallback.error);
            assertNotNull(kinveyListCallback.result);
            assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_" + skip);
            assertEquals(kinveyListCallback.result.get(1).getUsername(), "Person_" + (skip+1));
            skip += limit;
        }

        query = client.query();
        query.setLimit(5);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 5);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_0");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_4");


        query = client.query();
        query.setSkip(5);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 5);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_5");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_9");

        query = client.query();
        query.setLimit(6);
        query.setSkip(6);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 4);
        assertEquals(kinveyListCallback.result.get(0).getUsername(), "Person_6");
        assertEquals(kinveyListCallback.result.get(kinveyListCallback.result.size()-1).getUsername(), "Person_9");


        query = client.query();
        query.setSkip(10);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 0);

        query = client.query();
        query.setSkip(11);
        kinveyListCallback = find(store, query, 60);
        assertNull(kinveyListCallback.error);
        assertNotNull(kinveyListCallback.result);
        assertTrue(kinveyListCallback.result.size() == 0);


        DefaultKinveyPushCallback pushCallback = push(store, 60);
        assertNull(pushCallback.error);
        assertNotNull(pushCallback.result);
        assertTrue(pushCallback.result.getSuccessCount() == 10);

        skip = 0;
        for (int i = 0; i < 5; i++) {
            query = client.query();
            query.equals("_acl.creator", user.getId());
            query.setSkip(skip);
            query.setLimit(limit);

            DefaultKinveyPullCallback pullCallback = pull(store, query);
            assertNull(pullCallback.error);
            assertNotNull(pullCallback.result);
            assertTrue(pullCallback.result.getResult().size() == limit);
            assertNotNull(pullCallback.result.getResult().get(0));
            assertEquals(pullCallback.result.getResult().get(0).getUsername(), "Person_" + skip);
            assertEquals(pullCallback.result.getResult().get(pullCallback.result.getResult().size()-1).getUsername(), "Person_" + (skip+1));
            skip += limit;
        }

    }

    class ChangeTimeout implements HttpRequestInitializer {
        public void initialize(HttpRequest request) throws SocketTimeoutException {
            throw new SocketTimeoutException();
        }
    }

}

