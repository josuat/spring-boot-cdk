package com.josuat.userapi.controller;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Getter
public class ApiError {
  private HttpStatus status;
  private Date timestamp;
  private String error;

  public ApiError(HttpStatus status, String error) {
    this.status = status;
    this.error = error;
    this.timestamp = new Date();
  }
}
