import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MemberResourceRESTServiceTest {

  @BeforeAll
  public static void setup() {
    String url =
        System.getProperty(
            "base.url",
            System.getenv()
                .getOrDefault("BASE_URL", "http://kitchensink.local:8080/kitchensink/rest"));
    URI uri = URI.create(url);

    baseURI = uri.getScheme() + "://" + uri.getHost();
    port = uri.getPort() == -1 ? 80 : uri.getPort();
    basePath = uri.getPath();
  }

  private int currentMemberCount() {
    return when().get("/members").then().statusCode(200).extract().jsonPath().getList("$").size();
  }

  private String randomEmail() {
    return UUID.randomUUID().toString() + "@mailinator.com";
  }

  private String memberJson(String email) {
    return String.format(
        "{\"name\":\"Jane Doe\",\"email\":\"%s\",\"phoneNumber\":\"2125551234\"}", email);
  }

  @Test
  public void testRegisterMemberAndValidateFields() {
    int before = currentMemberCount();
    String email = randomEmail();

    // 1) Create the member
    given()
        .contentType("application/json")
        .body(memberJson(email))
        .when()
        .post("/members")
        .then()
        .statusCode(200)
        .body(isEmptyString());

    // 2) Ensure count increased by 1
    assertEquals(before + 1, currentMemberCount());

    // 3) Fetch all members and assert exact fields for the newly created one
    when()
        .get("/members")
        .then()
        .statusCode(200)
        // find the object with our email and assert its properties:
        .body("find { it.email == '%s' }.id", withArgs(email), notNullValue())
        .body("find { it.email == '%s' }.name", withArgs(email), equalTo("Jane Doe"))
        .body("find { it.email == '%s' }.email", withArgs(email), equalTo(email))
        .body("find { it.email == '%s' }.phoneNumber", withArgs(email), equalTo("2125551234"));
  }

  @Test
  public void testRegisterDuplicateMemberKeepsCount() {
    String email = randomEmail();
    String json = memberJson(email);

    // First registration: OK
    given()
        .contentType("application/json")
        .body(json)
        .when()
        .post("/members")
        .then()
        .statusCode(200);

    int before = currentMemberCount();

    // Second registration: conflict, same email
    given()
        .contentType("application/json")
        .body(json)
        .when()
        .post("/members")
        .then()
        .statusCode(409)
        .body("email", equalTo("Email taken"));

    // And no new row was inserted
    assertEquals(before, currentMemberCount());
  }

  @Test
  public void testListAllMembersIncludesExisting() {
    // Pre-seed one member for this test
    String email = randomEmail();
    given()
        .contentType("application/json")
        .body(memberJson(email))
        .when()
        .post("/members")
        .then()
        .statusCode(200);

    // List and verify at least one entry matches our seed
    when()
        .get("/members")
        .then()
        .statusCode(200)
        .body("findAll { it.email == '%s' }.size()", withArgs(email), greaterThanOrEqualTo(1));
  }
}
