package com.josuat.userapi.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Getter
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter
  private Long id;

  private Title title;

  @NotEmpty(message = "firstName is required for User")
  private String firstName;

  @NotEmpty(message = "lastName is required for User")
  private String lastName;

  private String mobileNumber;

  @OneToOne(cascade = CascadeType.ALL)
  private Address address;
}
