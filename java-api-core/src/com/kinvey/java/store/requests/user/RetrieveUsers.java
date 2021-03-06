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

package com.kinvey.java.store.requests.user;

import com.google.api.client.util.Key;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStoreRequestManager;

import java.lang.reflect.Array;

/**
 * Retrieve Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request object for
 * Retrieve User requests.
 */
public final class RetrieveUsers extends AbstractKinveyJsonClientRequest<User[]> {
    private static final String REST_PATH = "user/{appKey}/{userID}{?query,sort,limit,skip,resolve,resolve_depth,retainReference}";

    private UserStoreRequestManager userStoreRequestManager;
    @Key
    private String userID;
    @Key("query")
    private String queryFilter;
    @Key("sort")
    private String sortFilter;
    @Key
    private String limit;
    @Key
    private String skip;

    @Key("resolve")
    private String resolve;
    @Key("resolve_depth")
    private String resolve_depth;
    @Key("retainReferences")
    private String retainReferences;

    public RetrieveUsers(UserStoreRequestManager userStoreRequestManager, Query query){
        super(userStoreRequestManager.getClient(), "GET", REST_PATH, null, User[].class);
        this.userStoreRequestManager = userStoreRequestManager;
        this.queryFilter = query.getQueryFilterJson(userStoreRequestManager.getClient().getJsonFactory());
        int queryLimit = query.getLimit();
        int querySkip = query.getSkip();
        this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
        this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
        this.sortFilter = query.getSortString();
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStoreRequestManager.getClientAppVersion());
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson()
                    .toJson(userStoreRequestManager.getCustomRequestProperties()) );
        }
    }

    public RetrieveUsers(UserStoreRequestManager userStoreRequestManager, Query query, String[] resolve, int resolve_depth, boolean retain){
        super(userStoreRequestManager.getClient(), "GET", REST_PATH, null, User[].class);
        this.userStoreRequestManager = userStoreRequestManager;
        this.queryFilter = query.getQueryFilterJson(userStoreRequestManager.getClient().getJsonFactory());
        int queryLimit = query.getLimit();
        int querySkip = query.getSkip();
        this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
        this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
        this.sortFilter = query.getSortString();

        this.resolve = Joiner.on(",").join(resolve);
        this.resolve_depth = resolve_depth > 0 ? Integer.toString(resolve_depth) : null;
        this.retainReferences = Boolean.toString(retain);
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStoreRequestManager.getClientAppVersion());
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson()
                    .toJson(userStoreRequestManager.getCustomRequestProperties()) );
        }

    }
}
