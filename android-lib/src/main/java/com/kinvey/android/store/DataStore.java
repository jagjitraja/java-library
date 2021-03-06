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
package com.kinvey.android.store;


import com.google.api.client.json.GenericJson;
import com.google.common.base.Preconditions;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.AsyncPullRequest;
import com.kinvey.android.KinveyCallbackHandler;
import com.kinvey.android.async.AsyncPushRequest;
import com.kinvey.android.async.AsyncRequest;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyPurgeCallback;
import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Logger;
import com.kinvey.java.Query;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.BaseDataStore;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps the {@link BaseDataStore} public methods in asynchronous functionality using native Android AsyncTask.
 * <p/>
 * <p>
 * This functionality can be accessed through the {@link DataStore} convenience method.  BaseDataStore
 * gets and saves and sync entities that extend {@link com.google.api.client.json.GenericJson}.  A class that extends GenericJson
 * can map class members to KinveyCollection properties using {@link com.google.api.client.util.Key} attributes.  For example,
 * the following will map a string "city" to a Kinvey collection attributed named "city":
 * </p>
 * <p>
 * <pre>
 *     {@literal @}Key
 *     private String city;
 * </pre>
 * </p>
 * <p>
 * The @Key attribute also can take an optional name, which will map the member to a different attribute name in the Kinvey
 * collection.
 * </p>
 * <p>
 * <pre>
 *     {@literal @}Key("_id")
 *     private String customerID;
 * </pre>
 * </p>
 * <p>
 * Methods in this API use either {@link KinveyListCallback} for retrieving entity sets,
 * {@link KinveyDeleteCallback} for deleting appData, or  the general-purpose
 * {@link KinveyClientCallback} used for retrieving single entites or saving Entities.
 * </p>
 * <p/>
 * <p>
 * Entity Set sample:
 * <pre>
 * {@code
 *     DataStore<EventEntity> dataStore = DataStore.collection("myCollection",EventEntity.class, myClient, StoreType.SYNC);
 *     dataStore.find(myClient.query(), new KinveyListCallback<EventEntity> {
 *         public void onFailure(Throwable t) { ... }
 *         public void onSuccess(List<EventEntity> entities) { ... }
 *     });
 * }
 * </pre>
 * </p>
 * <p/>
 *
 * @author mjsalinger
 * @author edwardf
 * @version $Id: $
 * @since 2.0
 */
public class DataStore<T extends GenericJson> extends BaseDataStore<T> {


    //Every AbstractClient Request wrapper provided by the core NetworkManager gets a KEY here.
    //The below declared methodMap will map this key to a an appropriate method wrapper in the core NetworkManager.
    //This makes it very easy to add new wrappers, and allows for a single implementation of an async client request.
    private static final String KEY_GET_BY_ID = "KEY_GET_BY_ID";
    private static final String KEY_GET_BY_QUERY = "KEY_GET_BY_QUERY";
    private static final String KEY_GET_ALL = "KEY_GET_ALL";
    private static final String KEY_GET_BY_IDS = "KEY_GET_BY_IDS";

    private static final String KEY_DELETE_BY_ID = "KEY_DELETE_BY_ID";
    private static final String KEY_DELETE_BY_QUERY = "KEY_DELETE_BY_QUERY";
    private static final String KEY_DELETE_BY_IDS = "KEY_DELETE_BY_IDS";

    private static final String KEY_PURGE = "KEY_PURGE";


    /*private static final String KEY_GET_BY_ID_WITH_REFERENCES = "KEY_GET_BY_ID_WITH_REFERENCES";
    private static final String KEY_GET_QUERY_WITH_REFERENCES = "KEY_GET_QUERY_WITH_REFERENCES";
    private static final String KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER = "KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER";
    private static final String KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER = "KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER";
    

    private static final String KEY_COUNT = "KEY_COUNT";
    private static final String KEY_SUM = "KEY_SUM";
    private static final String KEY_MAX = "KEY_MAX";
    private static final String KEY_MIN = "KEY_MIN";
    private static final String KEY_AVERAGE = "KEY_AVERAGE";*/

    private static Map<String, Method> methodMap;


    /**
     * Constructor to instantiate the NetworkManager class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */
    protected DataStore(String collectionName, Class<T> myClass, AbstractClient client, StoreType storeType) {
        super(client, collectionName, myClass, storeType);
        loadMethodMap();
    }

    /**
     * Constructor to instantiate the NetworkManager class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */
    public DataStore(String collectionName, Class<T> myClass, AbstractClient client, StoreType storeType, NetworkManager<T> networkManager) {
        super(client, collectionName, myClass, storeType, networkManager);
        loadMethodMap();
    }

    public static <T extends GenericJson> DataStore<T> collection(String collectionName, Class<T> myClass, StoreType storeType, AbstractClient client) {
        Preconditions.checkNotNull(collectionName, "collectionName cannot be null.");
        Preconditions.checkNotNull(storeType, "storeType cannot be null.");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        return new DataStore<T>(collectionName, myClass, client, storeType);
    }

    private void loadMethodMap() {
        Map<String, Method> tempMap = new HashMap<String, Method>();
        try {
            tempMap.put(KEY_GET_BY_ID, BaseDataStore.class.getMethod("find", String.class, KinveyCachedClientCallback.class));
            tempMap.put(KEY_GET_BY_QUERY, BaseDataStore.class.getMethod("find", Query.class, KinveyCachedClientCallback.class));
            tempMap.put(KEY_GET_ALL, BaseDataStore.class.getMethod("find", KinveyCachedClientCallback.class));
            tempMap.put(KEY_GET_BY_IDS, BaseDataStore.class.getMethod("find", Iterable.class, KinveyCachedClientCallback.class));

            tempMap.put(KEY_DELETE_BY_ID, BaseDataStore.class.getMethod("delete", String.class));
            tempMap.put(KEY_DELETE_BY_QUERY, BaseDataStore.class.getMethod("delete", Query.class));
            tempMap.put(KEY_DELETE_BY_IDS, BaseDataStore.class.getMethod("delete", Iterable.class));

            tempMap.put(KEY_PURGE, BaseDataStore.class.getMethod("purge"));


            /*tempMap.put(KEY_GET_BY_ID_WITH_REFERENCES, NetworkManager.class.getMethod("getEntityBlocking", new Class[]{String.class, String[].class, int.class, boolean.class}));
            tempMap.put(KEY_GET_QUERY_WITH_REFERENCES, NetworkManager.class.getMethod("getBlocking", new Class[]{Query.class, String[].class, int.class, boolean.class}));
            tempMap.put(KEY_GET_BY_ID_WITH_REFERENCES_WRAPPER, NetworkManager.class.getMethod("getEntityBlocking", new Class[]{String.class, String[].class} ));
            tempMap.put(KEY_GET_BY_QUERY_WITH_REFERENCES_WRAPPER, NetworkManager.class.getMethod("getBlocking", new Class[]{Query.class, String[].class}));*/


        } catch (NoSuchMethodException e) {
        	Logger.ERROR("CHECK METHOD MAP, no such method is declared in NetworkManager!");
//            e.printStackTrace();
        }

        methodMap = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Asynchronous request to fetch a single Entity by ID.
     * <p>
     * Constructs an asynchronous request to fetch a single Entity by its Entity ID.  Returns an instance of that Entity
     * via KinveyClientCallback<T>
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC).find("123",
     *             new KinveyClientCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entityID entityID to fetch
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void find(String entityID, KinveyClientCallback<T> callback)  {
        find(entityID, callback, null);
    }


    /**
     * Asynchronous request to fetch a single Entity by ID with additional callback with data from cache.
     * <p>
     * Constructs an asynchronous request to fetch a single Entity by its Entity ID.  Returns an instance of that Entity
     * via KinveyClientCallback<T>
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.CACHE).find("123",
     *             new KinveyClientCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity entity) { ... }
     *     }, new KinveyCachedClientCallback<EventEntity>(){
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entityID entityID to fetch
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     *
     */
    public void find(String entityID, KinveyClientCallback<T> callback, final KinveyCachedClientCallback<T> cachedCallback)  {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(entityID, "entityID must not be null.");
        new AsyncRequest<T>(this, methodMap.get(KEY_GET_BY_ID), callback, entityID, getWrappedCacheCallback(cachedCallback)).execute();
    }

    /**
     * Asynchronous request to fetch an list of Entities using an list of _ids.
     * <p>
     * Constructs an asynchronous request to fetch an List of Entities, filtering by the provided list of _ids.  Uses
     * KinveyListCallback<T> to return an List of type T.  This method uses a Query {@link Query}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     myAppData.find(Lists.asList(new String[]{"189472023", "10193583"}), new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param ids A list of _ids to query by.
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void find(Iterable<String> ids, KinveyListCallback<T> callback){
        find(ids, callback, null);
    }

    /**
     * Asynchronous request to fetch an list of Entities using an list of _ids.
     * <p>
     * Constructs an asynchronous request to fetch an List of Entities, filtering by the provided list of _ids.  Uses
     * KinveyListCallback<T> to return an List of type T.  This method uses a Query {@link Query}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.CACHE);
     *     myAppData.find(Lists.asList(new String[]{"189472023", "10193583"}), new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     }, new KinveyCachedListCallback<EventEntity>(){
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param ids A list of _ids to query by.
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    public void find(Iterable<String> ids, KinveyListCallback<T> callback, KinveyCachedClientCallback<List<T>> cachedCallback){
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(ids, "ids must not be null.");
        new AsyncRequest<List<T>>(this, methodMap.get(KEY_GET_BY_IDS), callback, ids,
                getWrappedCacheCallback(cachedCallback)).execute();
    }


    /**
     * Asynchronous request to fetch an list of Entities using a Query object.
     * <p>
     * Constructs an asynchronous request to fetch an List of Entities, filtering by a Query object.  Uses
     * KinveyListCallback<T> to return an List of type T.  Queries can be constructed with {@link Query}.
     * An empty Query object will return all items in the collection.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     Query myQuery = myAppData.query();
     *     myQuery.equals("age",21);
     *     myAppData.find(myQuery, new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void find(Query query, KinveyListCallback<T> callback){
        find(query, callback, null);
    }


    /**
     * Asynchronous request to fetch an list of Entities using a Query object.
     * <p>
     * Constructs an asynchronous request to fetch an List of Entities, filtering by a Query object.  Uses
     * KinveyListCallback<T> to return an List of type T.  Queries can be constructed with {@link Query}.
     * An empty Query object will return all items in the collection.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.CACHE);
     *     Query myQuery = myAppData.query();
     *     myQuery.equals("age",21);
     *     myAppData.find(myQuery, new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     }, new KinveyCachedListCallback<EventEntity>(){
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    public void find(Query query, KinveyListCallback<T> callback, KinveyCachedClientCallback<List<T>> cachedCallback){
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(query, "Query must not be null.");
        new AsyncRequest<List<T>>(this, methodMap.get(KEY_GET_BY_QUERY), callback, query,
                getWrappedCacheCallback(cachedCallback)).execute();
    }

    /**
     * Asynchronous request to fetch an list of all Entities in a collection.
     * <p>
     * Constructs an asynchronous request to fetch an List of all entities in a collection.  Uses
     * KinveyListCallback<T> to return an List of type T.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     myAppData.find(new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback either successfully returns list of resolved entities or an error
     */
    public void find(KinveyListCallback<T> callback) {
        find(callback, null);
    }


    /**
     * Asynchronous request to fetch an list of all Entities in a collection.
     * <p>
     * Constructs an asynchronous request to fetch an List of all entities in a collection.  Uses
     * KinveyListCallback<T> to return an List of type T.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     myAppData.find(new KinveyListCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     }, new KinveyCachedListCallback<EventEntity>(){
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(List<EventEntity> entity) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback either successfully returns list of resolved entities or an error
     * @param cachedCallback either successfully returns list of resolved entities from cache or an error
     */
    public void find(KinveyListCallback<T> callback, KinveyCachedClientCallback<List<T>> cachedCallback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        new AsyncRequest<List<T>>(this, methodMap.get(KEY_GET_ALL), callback, getWrappedCacheCallback(cachedCallback)).execute();
    }


    /**
     * Asynchronous request to save or update an entity to a collection.
     * <p>
     * Constructs an asynchronous request to save an entity of type T to a collection.  Creates the entity if it doesn't exist, updates it if it does exist.
     * If an "_id" property is not present, the Kinvey backend will generate one.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     myAppData.save(entityID, new KinveyClientCallback<EventEntity> {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entity The entity to save
     * @param callback KinveyClientCallback<T>
     */
    public void save(T entity, KinveyClientCallback<T> callback)  {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(entity, "Entity cannot be null.");
        new SaveRequest(entity, callback).execute();

    }

    /**
     * Asynchronous request to delete an entity to a collection.
     * <p>
     * Creates an asynchronous request to delete a group of entities from a collection based on a Query object.  Uses KinveyDeleteCallback to return a
     * {@link com.kinvey.java.model.KinveyDeleteResponse}.  Queries can be constructed with {@link Query}.
     * An empty Query object will delete all items in the collection.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     myAppData.delete(myQuery, new KinveyDeleteCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entityID the ID to delete
     * @param callback KinveyDeleteCallback
     */
    public void delete(String entityID, KinveyDeleteCallback callback) {
        new AsyncRequest<Integer>(this, methodMap.get(KEY_DELETE_BY_ID), callback, entityID).execute();
    }


    /**
     * Asynchronous request to delete an entities from a collection.
     * <p>
     * Creates an asynchronous request to delete a group of entities from a collection based on a passed entities ids.  Uses KinveyDeleteCallback to return a
     * {@link com.kinvey.java.model.KinveyDeleteResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = kDataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     List<String> ids = ...
     *     myAppData.delete(ids, new KinveyDeleteCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param entityIDs the ID to delete
     * @param callback KinveyDeleteCallback
     */
    public void delete(Iterable<String> entityIDs, KinveyDeleteCallback callback) {
        new AsyncRequest<Integer>(this, methodMap.get(KEY_DELETE_BY_IDS), callback, entityIDs).execute();
    }

    /**
     * Asynchronous request to delete a collection of entites from a collection by Query.
     * <p>
     * Creates an asynchronous request to delete an entity from a  collection by Entity ID.  Uses KinveyDeleteCallback to return a
     * {@link com.kinvey.java.model.KinveyDeleteResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     Query myQuery = new Query();
     *     myQuery.equals("age",21);
     *     myAppData.delete(myQuery, new KinveyDeleteCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(EventEntity[] entities) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param callback KinveyDeleteCallback
     */
    public void delete(Query query, KinveyDeleteCallback callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        Preconditions.checkNotNull(query, "query cannot be null.");
        new AsyncRequest<Integer>(this, methodMap.get(KEY_DELETE_BY_QUERY), callback, query).execute();

    }

    /**
     * Asynchronous request to push a collection of entites to backend.
     * <p>
     * Creates an asynchronous request to push a collection of entites.  Uses KinveyPushCallback to return a
     * {@link KinveyPushResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     myAppData.push(new KinveyPushCallback() {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPushResponse kinveyPushResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback KinveyPushCallback
     */
    public void push(KinveyPushCallback callback){
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        SyncManager syncManager = client.getSycManager();
        new AsyncPushRequest(getCollectionName(), client.getSycManager(), client, storeType, callback).execute();
    }

    /**
     * Asynchronous request to pull a collection of entites from backend.
     * <p>
     * Creates an asynchronous request to pull an entity from backend.  Uses KinveyPullCallback<T> to return a
     * {@link KinveyPullResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     Query myQuery = new Query();
     *     myQuery.equals("age",21);
     *     myAppData.pull(myQuery, new KinveyPullCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results.
     * @param callback KinveyPullCallback
     */
    public void pull(Query query, KinveyPullCallback<T> callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        SyncManager syncManager = client.getSycManager();
        new AsyncPullRequest<T>(this, query, callback).execute();
    }

    /**
     * Asynchronous request to pull a collection of entites from backend.
     * <p>
     * Creates an asynchronous request to pull all entity from backend.  Uses KinveyPullCallback<T> to return a
     * {@link KinveyPullResponse}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     myAppData.pull(new KinveyPullCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(KinveyPullResponse kinveyPullResponse) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback KinveyPullCallback
     */
    public void pull(KinveyPullCallback<T> callback) {
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        SyncManager syncManager = client.getSycManager();
        new AsyncPullRequest<T>(this, null, callback).execute();
    }

    /**
     * Asynchronous request to clear cache from a collection of entity and pull all collection.
     * <p>
     * Creates an asynchronous request to clear cache from a collection of entity and pull all collection.
     * Uses KinveyPullCallback<T> to return a {@link KinveyPurgeCallback}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     myAppData.purge(new KinveyPurgeCallback {
     *         public void onFailure(Throwable t) { ... }
     *         public void onSuccess(Void result) { ... }
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param callback KinveyPurgeCallback
     */
    public void purge(KinveyPurgeCallback callback){
        Preconditions.checkNotNull(client, "client must not be null");
        Preconditions.checkArgument(client.isInitialize(), "client must be initialized.");
        SyncManager syncManager = client.getSycManager();
        new AsyncRequest<Void>(this, methodMap.get(KEY_PURGE), callback).execute();
    }


    /**
     * Asynchronous request to sync a collection of entites from a network collection by Query.
     * <p>
     * Creates an asynchronous request to sync local entities and network entries matched query from
     * a given collection by Query.  Uses KinveySyncCallback to return a
     * {@link com.kinvey.android.sync.KinveySyncCallback}.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *     DataStore<EventEntity> myAppData = DataStore.collection("myCollection", EventEntity.class, myClient, StoreType.SYNC);
     *     Query myQuery = new Query();
     *     myQuery.equals("age",21);
     *     myAppData.sync(myQuery, new KinveySyncCallback {
     *     public void onSuccess(KinveyPushResponse kinveyPushResponse,
     *         KinveyPullResponse<T> kinveyPullResponse) {...}
     *         void onSuccess(){...};
     *         void onPullStarted(){...};
     *         void onPushStarted(){...};
     *         void onPullSuccess(){...};
     *         void onPushSuccess(){...};
     *         void onFailure(Throwable t){...};
     *
     *     });
     * }
     * </pre>
     * </p>
     *
     * @param query {@link Query} to filter the results or null if you don't want to query.
     * @param callback KinveyDeleteCallback
     */
    public void sync(final Query query, final KinveySyncCallback<T> callback) {
        callback.onPushStarted();
        push(new KinveyPushCallback() {
            @Override
            public void onSuccess(final KinveyPushResponse pushResult) {
                callback.onPushSuccess(pushResult);
                callback.onPullStarted();
                DataStore.this.pull(query, new KinveyPullCallback<T>() {

                    @Override
                    public void onSuccess(KinveyPullResponse<T> pullResult) {
                        callback.onPullSuccess(pullResult);
                        callback.onSuccess(pushResult, pullResult);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        callback.onFailure(error);

                    }
                });
            }

            @Override
            public void onFailure(Throwable error) {

                callback.onFailure(error);
            }

            @Override
            public void onProgress(long current, long all) {

            }
        });
    }

    /**
     * Alias for {@link #sync(Query, KinveySyncCallback)} where query equals null
     *
     * @param callback callback to notify working thread on operation status update
     */
    public void sync(final KinveySyncCallback<T> callback) {
        sync(null, callback);
    }

    public Query query() {
        return new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
    }

    private class SaveRequest extends AsyncClientRequest<T> {
        T entity;

        public SaveRequest(T entity, KinveyClientCallback<T> callback) {
            super(callback);
            this.entity = entity;
        }

        @Override
        protected T executeAsync() throws IOException {
            return (DataStore.super.save(entity));
        }
    }

    private static <T> KinveyCachedClientCallback<T> getWrappedCacheCallback(KinveyCachedClientCallback<T> callback) {
        KinveyCachedClientCallback<T> ret = null;
        if (callback != null) {
            ret = new ThreadedKinveyCachedClientCallback<T>(callback);
        }
        return ret;
    }

    private static class ThreadedKinveyCachedClientCallback<T> implements KinveyCachedClientCallback<T> {

        private KinveyCachedClientCallback<T> callback;
        KinveyCallbackHandler<T> handler;


        ThreadedKinveyCachedClientCallback(KinveyCachedClientCallback<T> callback) {
            handler = new KinveyCallbackHandler<T>();
            this.callback = callback;
        }

        @Override
        public void onSuccess(T result) {
            handler.onResult(result, callback);
        }

        @Override
        public void onFailure(Throwable error) {
            handler.onFailure(error, callback);

        }
    }

}
