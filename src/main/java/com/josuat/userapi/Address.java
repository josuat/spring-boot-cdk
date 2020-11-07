package com.josuat.userapi;

import lombok.Getter;

@Getter
public class Address {
  private String postcode;
  private String suburb;
  private State state;
  private String fullAddress;
}
