/*
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;

import java.io.IOException;
import java.util.HashMap;

/**
 * Class for managing access to custom RPC endpoints.
 *
 * @author edwardf
 * @since 2.0.2
 */
public class CustomEndpoints<I, R> {


    private AbstractClient client;

    public CustomEndpoints(AbstractClient client){
        this.client = client;
    }

    public RpcCommand runCommandBlocking(String commandName, HashMap<String, String> args) throws IOException{
        Preconditions.checkNotNull(commandName, "commandName must not be null");
        RpcCommand command = new RpcCommand(commandName, args,  GenericJson.class);
        client.initializeRequest(command);
        return command;



    }





    public class RpcCommand extends AbstractKinveyJsonClientRequest<R> {
        private static final String REST_PATH = "rpc/{appKey}/custom/{endpoint}";

        @Key
        private String endpoint;


        RpcCommand(String commandName, Object args, Class responseClass) {
            super(client, "POST", REST_PATH, args, responseClass);
            this.endpoint = commandName;
        }


    }


}