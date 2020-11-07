package com.josuat.userapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

  @Test
	@Order(1)
	void testCreateUser() throws Exception {
    mockMvc.perform(
        post("/users").
            contentType("application/json").
            content(String.format("{%s}", userJsonContent))).
        andExpect(status().isOk()).
        andExpect(content().contentType(MediaType.APPLICATION_JSON)).
        andExpect(content().json("{\"id\": 1, \"firstName\": \"John\"}"));
  }

  @Test
	@Order(2)
	void testGetUser() throws Exception {
		mockMvc.perform(
				get("/users/1").
						contentType("application/json")).
				andExpect(status().isOk()).
				andExpect(content().contentType(MediaType.APPLICATION_JSON)).
				andExpect(content().json("{\"id\": 1, " + userJsonContent + "}"));
	}

	@Test
	@Order(3)
	void testUpdateUser() throws Exception {
		var updatedUser = userJsonContent.replace("047777777", "0404111222");

  	mockMvc.perform(
				put("/users/1").
						contentType("application/json").
						content(String.format("{%s}", updatedUser))).
				andExpect(status().isOk()).
				andExpect(content().contentType(MediaType.APPLICATION_JSON)).
				andExpect(content().json("{\"id\": 1, \"mobileNumber\": \"0404111222\"}"));
	}

	//TODO test invalid inputs

	//TODO test repository state once implemented
}
