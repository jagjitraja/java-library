package com.kinvey.java.store.requests.data.read;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Prots on 2/15/16.
 */
public class ReadQueryRequest<T extends GenericJson> extends AbstractReadRequest<T> {

    private final Query query;

    public ReadQueryRequest(ICache<T> cache, NetworkManager<T> networkManager, ReadPolicy readPolicy,
                            Query query) {
        super(cache, readPolicy, networkManager);
        this.query = query;
    }

    @Override
    protected List<T> getCached() {
        return cache.get(query);
    }

    @Override
    protected List<T> getNetwork() throws IOException {
        return Arrays.asList(getNetworkData().getBlocking(query).execute());
    }
}
