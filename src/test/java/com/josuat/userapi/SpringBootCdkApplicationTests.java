package com.josuat.userapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josuat.userapi.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpringBootCdkApplicationTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  String userJsonContent =
      "    \"title\": \"Mr\"," +
          "    \"firstName\": \"John\"," +
          "    \"lastName\": \"P\"," +
          "    \"mobileNumber\": \"047777777\"," +
          "    \"address\": {" +
          "        \"postcode\": \"2040\"," +
          "        \"suburb\": \"LEICHHARDT\"," +
          "        \"state\": \"NSW\"," +
          "        \"fullAddress\": \"100 william street, leichhdart, nsw 2040\"" +
          "    }";

  static HttpHeaders authHeaders = new HttpHeaders();

  @BeforeAll
  public static void setUp() {
    String token = new String(Base64.getEncoder().encode(("testUser" + ":" + "testPass").getBytes()));
    authHeaders.set("Authorization", "Basic " + token);
  }

  @Test
  @Order(1)
  void verifyUserRepoEmpty() {
    assertFalse(userRepository.findAll().iterator().hasNext());
  }

  @Test
  @Order(2)
  void getNonExistentUser() throws Exception {
    mockMvc.perform(
        get("/users/1").
            contentType("application/json").
            headers(authHeaders)).
        andExpect(status().is(404));
  }

  @Test
  @Order(10)
  void createUser() throws Exception {
    mockMvc.perform(
        post("/users").
            contentType("application/json").
            headers(authHeaders).
            content(String.format("{%s}", userJsonContent))).
        andExpect(status().isOk()).
        andExpect(content().contentType(MediaType.APPLICATION_JSON)).
        andExpect(content().json("{\"id\": 1, \"firstName\": \"John\"}"));
  }

  @Test
  @Order(11)
  void getUser() throws Exception {
    mockMvc.perform(
        get("/users/1").
            contentType("application/json").
            headers(authHeaders)).
        andExpect(status().isOk()).
        andExpect(content().contentType(MediaType.APPLICATION_JSON)).
        andExpect(content().json("{\"id\": 1, " + userJsonContent + "}"));
  }

  @Test
  @Order(12)
  void updateUser() throws Exception {
    var updatedUser = userJsonContent.replace("047777777", "0404111222");

    mockMvc.perform(
        put("/users/1").
            contentType("application/json").
            headers(authHeaders).
            content(String.format("{%s}", updatedUser))).
        andExpect(status().isOk()).
        andExpect(content().contentType(MediaType.APPLICATION_JSON)).
        andExpect(content().json("{\"id\": 1, \"mobileNumber\": \"0404111222\"}"));
  }

  //TODO test invalid inputs

  @Test
  @Order(20)
  void verifyUserRepoState() {
		Optional<User> optionalUser = userRepository.findById(1L);
		assertTrue(optionalUser.isPresent());
		User user = optionalUser.get();
		assertEquals(user.getFirstName(), "John");
		assertEquals(user.getMobileNumber(), "0404111222");
		assertEquals(user.getAddress().getPostcode(), "2040");
	}
}
