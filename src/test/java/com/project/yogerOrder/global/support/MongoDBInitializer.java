package com.project.yogerOrder.global.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.mongodb.core.MongoTemplate;

@TestComponent
public class MongoDBInitializer {

    @Autowired
    MongoTemplate mongoTemplate;

    public void clear() {
        mongoTemplate.getCollectionNames().forEach(name -> mongoTemplate.getCollection(name).drop());
    }
}
