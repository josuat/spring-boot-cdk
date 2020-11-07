package com.josuat.userapi.model;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
public class Address {
  @Id
  @GeneratedValue
  private Long id;

  private String postcode;
  private String suburb;
  private State state;
  private String fullAddress;
}
