package com.josuat.userapi.model;

import lombok.Getter;

@Getter
public class Address {
  private String postcode;
  private String suburb;
  private State state;
  private String fullAddress;
}
