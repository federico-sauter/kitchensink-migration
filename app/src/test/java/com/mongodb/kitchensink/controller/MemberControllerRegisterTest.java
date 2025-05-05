package com.mongodb.kitchensink.controller;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerRegisterTest {

  @LocalServerPort int port;

  private String base;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.basePath = "/kitchensink";
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void registerMember_success() {
    var json =
        Map.of(
            "name", "Alice",
            "email", "alice@mailinator.com",
            "phoneNumber", "1234567890");
    given()
        .contentType("application/json")
        .body(json)
        .when()
        .post("/rest/members")
        .then()
        .statusCode(200)
        .body(isEmptyString());

    given().port(port).when().get("/rest/members").then().statusCode(200).body("$", hasSize(1));
  }

  @Test
  void registerMember_duplicate() {
    var json =
        Map.of(
            "name", "Bob",
            "email", "bob@mailinator.com",
            "phoneNumber", "0987654321");
    // first
    given()
        .contentType("application/json")
        .body(json)
        .when()
        .post("/rest/members")
        .then()
        .statusCode(200);

    // duplicate
    given()
        .contentType("application/json")
        .body(json)
        .when()
        .post("/rest/members")
        .then()
        .statusCode(409)
        .body("email", equalTo("Email taken"));
  }
}
