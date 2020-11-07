package com.josuat.userapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

  private final Logger logger = LoggerFactory.getLogger(UserController.class);

  private long nextId = 1;
  private Map<Long, User> users = new HashMap<>();

  @GetMapping("/users/{id}")
  public User getUserDetails(@PathVariable long id) {
    if (users.containsKey(id)) {
      return users.get(id);
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Invalid user id %s", id));
    }
  }

  @PostMapping("/users")
  public User createUser(@RequestBody User user) {
    long userId = nextId++;
    user.setId(userId);
    users.put(userId, user);
    logger.info("==> Adding User " + user.getFirstName());
    return user;
  }

  @PutMapping("/users/{id}")
  public User updateUser(@PathVariable long id, @RequestBody User user) {
    if (users.containsKey(id)) {
      users.put(id, user);
      return user;
    }
    else throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Invalid user id %s", id));
  }
}
