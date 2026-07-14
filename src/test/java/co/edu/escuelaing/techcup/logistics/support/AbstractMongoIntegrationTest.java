package co.edu.escuelaing.techcup.logistics.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para pruebas que necesitan levantar el contexto completo de Spring
 * (por ejemplo {@code @SpringBootTest}) y por lo tanto requieren un Mongo
 * real. Usa Testcontainers para levantar un contenedor MongoDB efimero y lo
 * conecta automaticamente via {@code @ServiceConnection}, sin necesidad de
 * configurar manualmente {@code spring.data.mongodb.uri} en las pruebas.
 */
@Tag("integration")
@Testcontainers
@SpringBootTest
public abstract class AbstractMongoIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7");
}
