package com.josuat.userapi;

import com.josuat.userapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/users/{id}")
  public User getUserDetails(@PathVariable long id) {
    return userRepository.
        findById(id).
        orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Invalid user id %s", id))
        );
  }

  @PostMapping("/users")
  public User createUser(@RequestBody User user) {
    return userRepository.save(user);
  }

  @PutMapping("/users/{id}")
  public User updateUser(@PathVariable long id, @RequestBody User user) {
    if (userRepository.existsById(id)) {
      user.setId(id);
      return userRepository.save(user);
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Invalid user id %s", id));
    }
  }
}
