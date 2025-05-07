package com.mongodb.kitchensink.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document("members")
public class Member {

  @Id private String id;

  @NotNull(message = "Name must not be null")
  @Size(min = 1, max = 25, message = "Name length must be between 1 and 25")
  @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
  private String name;

  @NotNull(message = "Email must not be null")
  @NotEmpty(message = "Email must not be empty")
  @Email(message = "Must be a well-formed email address")
  private String email;

  @NotNull(message = "Phone number must not be null")
  @Size(min = 10, max = 12, message = "Phone number length must be between 10 and 12")
  @Digits(fraction = 0, integer = 12, message = "Phone number must be numeric")
  private String phoneNumber;

  // getters & setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }
}
