package com.josuat.userapi.model;

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
}
