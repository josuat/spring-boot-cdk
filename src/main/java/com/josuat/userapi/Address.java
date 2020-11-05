package com.josuat.userapi;

import lombok.Getter;

@Getter
public class Address {
  private int postcode;
  private String suburb;
  private State state;
  private String fullAddress;
}
