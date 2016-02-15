package com.kinvey.java.store.requests.data.read;

import com.google.api.client.json.GenericJson;
import com.google.common.collect.Iterables;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Prots on 2/15/16.
 */
public class ReadIdsRequest<T extends GenericJson> extends AbstractReadRequest<T> {
    private Iterable<String> ids;

    public ReadIdsRequest(AbstractClient client, String collectionName, Class<T> clazz, ICache<T> cache, ReadPolicy readPolicy,
                          Iterable<String> ids) {
        super(client, collectionName, clazz, cache, readPolicy);
        this.ids = ids;
    }

    @Override
    protected List<T> getCached() {
        return cache.get(ids);
    }

    @Override
    protected List<T> getNetwork() throws IOException {
        return
                Arrays.asList(
                        getNetworkData().getBlocking(Iterables.toArray(ids, String.class)).execute()
                );
    }
}
