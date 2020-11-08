package com.josuat.userapi;

import com.josuat.userapi.model.User;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@ControllerAdvice
public class UserController extends ResponseEntityExceptionHandler {

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/users/{id}")
  public User getUserDetails(@Valid @PathVariable long id) {
    return userRepository.
        findById(id).
        orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Invalid user id %s", id))
        );
  }

  @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
  public User createUser(@Valid @RequestBody User user) {
    return userRepository.save(user);
  }

  @PutMapping(value = "/users/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public User updateUser(@PathVariable long id, @Valid @RequestBody User user) {
    if (userRepository.existsById(id)) {
      user.setId(id);
      return userRepository.save(user);
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Invalid user id %s", id));
    }
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    var validationErrMsg = ex.getBindingResult().
        getAllErrors().stream().
        map(DefaultMessageSourceResolvable::getDefaultMessage).
        collect(Collectors.joining());
    ApiError apiError = new ApiError(status, validationErrMsg);
    return handleExceptionInternal(ex, apiError, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    ApiError apiError = new ApiError(status, "invalid request parameter, integer required!");
    return handleExceptionInternal(ex, apiError, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    ApiError apiError = new ApiError(status, ex.getMessage());
    return handleExceptionInternal(ex, apiError, headers, status, request);
  }

  @ExceptionHandler({ ResponseStatusException.class })
  public ResponseEntity<Object> handleAll(ResponseStatusException ex, WebRequest request) {
    ApiError apiError = new ApiError(ex.getStatus(), ex.getReason());
    return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  @ExceptionHandler({ Exception.class })
  public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
    ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error, please contact API maintainer.");
    return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
  }

}
