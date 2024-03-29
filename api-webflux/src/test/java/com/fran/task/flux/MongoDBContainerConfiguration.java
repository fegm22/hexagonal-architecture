package com.fran.task.flux;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class MongoDBContainerConfiguration {

    @Container
    static final MongoDBContainer container;

    static {
        container = new MongoDBContainer(DockerImageName.parse("mongo:5.0.12"));
        container.start();
    }

    @DynamicPropertySource
    static void setMongoDbContainerProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", container::getHost);
        registry.add("spring.data.mongodb.port", container::getFirstMappedPort);
    }

}
