package ru.vladigeras.gridfs;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration {

    @Value("${fileStorage.host}")
    private String host;

    @Value("${fileStorage.port}")
    private String port;

    @Value("${fileStorage.database}")
    private String database;

    @Override
    protected String getDatabaseName() {
        return database;
    }
  
    @Override
    public MongoClient mongoClient() {
        return new MongoClient(host, Integer.parseInt(port));
    }
}
