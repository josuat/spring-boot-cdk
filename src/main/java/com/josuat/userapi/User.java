package com.josuat.userapi;

import lombok.Getter;
import lombok.Setter;

@Getter
public class User {
  @Setter
  private Long id;
  private Title title;
  private String firstName;
  private String lastName;
  private String mobileNumber;
  private Address address;

  public User(Title title, String firstName, String lastName, String mobileNumber, Address address) {
    this.title = title;
    this.firstName = firstName;
    this.lastName = lastName;
    this.mobileNumber = mobileNumber;
    this.address = address;
  }
}
