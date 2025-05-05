package com.mongodb.kitchensink.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerSliceTest {

  @LocalServerPort int port;

  @Test
  void getMembers_initial() {
    given()
        .port(port)
        .when()
        .get("/kitchensink/rest/members")
        .then()
        .statusCode(200)
        .body("$", hasSize(0)); // expect empty list
  }
}
