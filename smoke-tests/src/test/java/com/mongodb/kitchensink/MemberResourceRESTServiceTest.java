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
    // pick up BASE_URL system property or env var, default to local dev
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
    String email = UUID.randomUUID().toString() + "@mailinator.com";
    System.out.println(email);
    return email;
  }

  private String memberJson(String email) {
    return String.format(
        "{\"name\":\"Jane Doe\",\"email\":\"%s\",\"phoneNumber\":\"2125551234\"}", email);
  }

  @Test
  public void testRegisterMember() {
    int before = currentMemberCount();

    String email = randomEmail();
    given()
        .contentType("application/json")
        .body(memberJson(email))
        .when()
        .post("/members")
        .then()
        .statusCode(200)
        .header("Location", not(isEmptyString()));

    int after = currentMemberCount();
    assertEquals(before + 1, after);
  }

  @Test
  public void testListAllMembers() {
    // use a fresh email here, too
    String email = randomEmail();
    given()
        .contentType("application/json")
        .body(memberJson(email))
        .when()
        .post("/members")
        .then()
        .statusCode(200);

    when().get("/members").then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  public void testRegisterDuplicateMember() {
    // pick one email, use it twice
    String email = randomEmail();
    String json = memberJson(email);

    // first should succeed
    given()
        .contentType("application/json")
        .body(json)
        .when()
        .post("/members")
        .then()
        .statusCode(200);

    int before = currentMemberCount();

    // second should conflict
    given()
        .contentType("application/json")
        .body(json)
        .when()
        .post("/members")
        .then()
        .statusCode(409);

    int after = currentMemberCount();
    assertEquals(before, after);
  }
}
