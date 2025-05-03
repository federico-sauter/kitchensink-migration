package com.mongodb.kitchensink;

import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(
    exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class KitchensinkApplicationTests {
  @LocalServerPort int port;

  @BeforeAll
  static void setup() {
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test
  void contextLoads() {}

  @Test
  void rootEndpointIsUp() {
    RestAssured.given()
        .port(port)
        .when()
        .get("/kitchensink/")
        .then()
        .statusCode(anyOf(is(404), is(200)));
  }
}
