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
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import com.kinvey.java.core.KinveyMockUnitTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class AppDataTest extends KinveyMockUnitTest {

    public class Entity extends GenericJson {

        @Key("_id")
        private String title;

        @Key("Name")
        private String name;

        public Entity() {}

        public Entity(String title) {
            super();
            this.title = title;
        }

        public Entity(String title, String name) {
            super();
            this.title = title;
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class EntityNoID extends GenericJson {

        @Key("Name")
        private String name;

        public EntityNoID() {}

        public EntityNoID(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public void testNewQuery() {
        Query myQuery = mockClient.query();
        assertEquals(0, myQuery.getLimit());
        assertEquals(0, myQuery.getSkip());
        assertEquals("",myQuery.getSortString());
        LinkedHashMap<String,Object> expected = new LinkedHashMap<String,Object>();
        assertEquals(expected ,(LinkedHashMap<String, Object>) myQuery.getQueryFilterMap());
    }

    // String collectionName, Class<T> myClass, AbstractClient client,KinveyClientRequestInitializer initializer
    public void testAppdataInitialization() {
        AppData<Entity> appData = new AppData<Entity>("testCollection",Entity.class,
                mockClient);
        assertEquals("testCollection",appData.getCollectionName());
        assertEquals(Entity.class, appData.getCurrentClass());
    }

    public void testNullCollectionInitialization() {
        try {
            AppData<Entity> appData = new AppData<Entity>(null, Entity.class, mockClient);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

    public void testNullClassInitialization() {
        AppData<Entity> appData = new AppData<Entity>("myCollection", null, mockClient);
        // Null class types are allowed, should not throw an exception.
        assertNull(appData.getCurrentClass());
    }

    public void testNullClientInitialization() {
        try {
            AppData<Entity> appData = new AppData<Entity>("myCollection", Entity.class, null);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException ex) {}
    }

//    public void testNullRequestInitializerInitialization() {
//        try {
//            AppData<Entity> appData = new AppData<Entity>("myCollection", Entity.class, mockClient);
//            fail("NullPointerException should be thrown.");
//        } catch (NullPointerException ex) {}
//    }

    public void testChangeCollectionName() {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        assertEquals("myCollection",appData.getCollectionName());
        appData.setCollectionName("myNewCollection");
        assertEquals("myNewCollection", appData.getCollectionName());
    }

    public void testChangeCollectionNameToNull() {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        assertEquals("myCollection", appData.getCollectionName());
        try {
            appData.setCollectionName(null);
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testGet() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        String entityID = "myEntity";
        AppData.GetEntity myGet = appData.getEntityBlocking(entityID);
        assertNotNull(myGet);
        assertEquals("myEntity", myGet.get("entityID"));
        assertEquals("myCollection",myGet.get("collectionName"));
        assertEquals("GET", myGet.getRequestMethod());
    }

    public void testGetWithNoEntityID() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        AppData.Get myGet = appData.getBlocking();
        assertNotNull(myGet);
        assertNull(myGet.get("entityID"));
        assertEquals("myCollection", myGet.get("collectionName"));
        assertEquals("GET", myGet.getRequestMethod());
    }

    public void testGetWithArrayType() throws IOException {
        Entity[] entityList = new Entity[]{};
        AppData<Entity[]> appData = getGenericAppData(entityList.getClass());
        AppData.Get myGet = appData.getBlocking();
        assertNotNull(myGet);
        assertNull(myGet.get("entityID"));
        assertEquals("myCollection",myGet.get("collectionName"));
        assertEquals("GET", myGet.getRequestMethod());
    }

    public void testSave() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        AppData<Entity>.Save mySave = appData.saveBlocking(entity);
        assertNotNull(mySave);
        assertEquals("myEntity", ((GenericJson) mySave.getJsonContent()).get("_id"));
        assertEquals("My Name", ((GenericJson) mySave.getJsonContent()).get("Name"));
        assertEquals("PUT",mySave.getRequestMethod());
    }

    public void testSaveNullEntity() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = null;
        try {
            AppData<Entity>.Save mySave = appData.saveBlocking(entity);
            fail("NullPointerException should be thrown.");
        } catch (NullPointerException ex) {}
    }

    public void testSaveNoID() throws IOException {
        AppData<EntityNoID> appData = getGenericAppData(EntityNoID.class);
        EntityNoID entity = new EntityNoID("My Name");

        AppData<EntityNoID>.Save mySave= appData.saveBlocking(entity);
        assertNull(((GenericJson) mySave.getJsonContent()).get("_id"));
        assertEquals("My Name",((GenericJson) mySave.getJsonContent()).get("Name"));
        assertEquals("POST",mySave.getRequestMethod());
    }

    public void testDelete() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        String entityID = "myEntity";
        AppData.Delete myDelete = appData.deleteBlocking(entityID);
        assertNotNull(myDelete);
        assertEquals("myEntity", myDelete.get("entityID"));
        assertEquals("myCollection",myDelete.get("collectionName"));
        assertEquals("DELETE", myDelete.getRequestMethod());
    }

//    public void testDeleteNullEntityID() throws IOException {
//        AppData<Entity> appData = getGenericAppData(Entity.class);
//        String entityID = "myEntity";
//        try {
//            AppData<Entity>.Delete<Entity> myDelete = appData.deleteBlocking(null);   TODO now ambigious because of query support...
//            fail("NullPointerException should be thrown.");
//        } catch (NullPointerException ex) {}
//
//    }

    public void testAggregateCountNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.countBlocking(fields, null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ out._result++;}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateCountCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.countBlocking(fields, query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ out._result++;}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAggregateSumNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.sumBlocking(fields, "total", null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ out._result= out._result + doc.total;}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateSumCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.sumBlocking(fields, "total", query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ out._result= out._result + doc.total;}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAggregateMaxNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.maxBlocking(fields, "total", null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result","-Infinity");

        String expectedReduce = "function(doc,out){ out._result = Math.maxBlocking(out._result, doc.total);}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateMaxCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.maxBlocking(fields, "total", query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result","-Infinity");

        String expectedReduce = "function(doc,out){ out._result = Math.maxBlocking(out._result, doc.total);}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAggregateMinNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.minBlocking(fields, "total", null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result","Infinity");

        String expectedReduce = "function(doc,out){ out._result = Math.minBlocking(out._result, doc.total);}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateMinCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.minBlocking(fields, "total", query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result","Infinity");

        String expectedReduce = "function(doc,out){ out._result = Math.minBlocking(out._result, doc.total);}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    public void testAggregateAverageNoCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        AppData<Entity>.Aggregate myAggregate = appData.averageBlocking(fields, "total", null);
        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ var countBlocking = (out._kcs_count == undefined) ? 0 : out._kcs_count; " +
            "out._result =(out._result * countBlocking + doc.total) " +
                    "/ (countBlocking + 1); out._kcs_count = countBlocking+1;}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
    }

    public void testAggregateAverageCondition() throws IOException {
        AppData<Entity> appData = getGenericAppData(Entity.class);
        Entity entity = new Entity("myEntity","My Name");
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("state");
        MockQuery query = new MockQuery(new MockQueryFilter.MockBuilder());

        AppData<Entity>.Aggregate myAggregate = appData.averageBlocking(fields, "total", query);

        HashMap<String,Boolean> expectedFields = new HashMap<String,Boolean>();
        expectedFields.put("state",true);

        HashMap<String,Object> expectedInitial = new HashMap<String,Object>();
        expectedInitial.put("_result",0);

        String expectedReduce = "function(doc,out){ var countBlocking = (out._kcs_count == undefined) ? 0 : out._kcs_count; " +
                "out._result =(out._result * countBlocking + doc.total) " +
                "/ (countBlocking + 1); out._kcs_count = countBlocking+1;}";

        String expectedCondition="{city=boston, age={$gt=18, $lt=21}}";

        assertNotNull(myAggregate);
        assertEquals(expectedFields, ((GenericJson) myAggregate.getJsonContent()).get("key"));
        assertEquals("POST",myAggregate.getRequestMethod());
        assertEquals(expectedInitial, ((GenericJson) myAggregate.getJsonContent()).get("initial"));
        assertEquals(expectedReduce, ((GenericJson) myAggregate.getJsonContent()).get("reduce"));
        assertEquals(expectedCondition, ((GenericJson) myAggregate.getJsonContent()).get("condition").toString());
    }

    private <T> AppData<T> getGenericAppData(Class<? extends Object> myClass) {
        AppData appData = new AppData("myCollection", myClass, mockClient);
        return appData;
    }


}
