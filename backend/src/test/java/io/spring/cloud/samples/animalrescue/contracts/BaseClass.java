package io.spring.cloud.samples.animalrescue.contracts;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import io.spring.cloud.samples.animalrescue.backend.AnimalRescueBackendApplication;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = AnimalRescueBackendApplication.class)
@AutoConfigureWebTestClient
public class BaseClass {

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	void setUp() {
		RestAssuredWebTestClient.webTestClient(webTestClient);
	}
}
