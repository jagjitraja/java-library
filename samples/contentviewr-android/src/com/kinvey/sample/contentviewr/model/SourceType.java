/*
 * Copyright (c) 2013 Kinvey Inc.
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
package com.kinvey.sample.contentviewr.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;

/**
 * @author edwardf
 */
public class SourceType extends GenericJson {

    @Key
    private TYPE type;


    //Dependant on type, this is either a URL or a file ID
    @Key
    private String reference;



    public SourceType(){}

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }


    public enum TYPE{
        @Value
        WEBSITE,
        @Value
        FILE}

}
