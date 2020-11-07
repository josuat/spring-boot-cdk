package com.josuat.userapi.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
public class User {
  @Id
  @GeneratedValue
  @Setter
  private Long id;

  private Title title;

  private String firstName;

  private String lastName;

  private String mobileNumber;

  @OneToOne(cascade = CascadeType.ALL)
  private Address address;
}
