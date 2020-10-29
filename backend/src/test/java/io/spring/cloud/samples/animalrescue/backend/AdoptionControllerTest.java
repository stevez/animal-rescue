package io.spring.cloud.samples.animalrescue.backend;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(controllers = AdoptionController.class)
class AdoptionControllerTest {

	private final String TEST_USER_NAME = "bob";

	@MockBean
	AdoptionService adoptionService;

	@Autowired
	WebTestClient webTestClient;

	@Test
	@WithMockUser(username = TEST_USER_NAME)
	void shouldAddAdoptionRequest() {
		AdoptionRequest request = new AdoptionRequest();
		request.setEmail("bob@example.com");
		request.setNotes("Test");

		this.webTestClient.mutateWith(csrf())
			.post().uri("/animals/1/adoption-requests")
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated();

		AdoptionRequest expectedRequest = new AdoptionRequest();
		expectedRequest.setEmail("bob@example.com");
		expectedRequest.setNotes("Test");
		expectedRequest.setAdopterName(TEST_USER_NAME);

		verify(adoptionService).add(eq(1L), refEq(expectedRequest));
	}

	@Test
	@WithMockUser(username = TEST_USER_NAME)
	void shouldUpdateAdoptionRequest() {
		AdoptionRequest request = new AdoptionRequest();
		request.setId(2L);
		request.setEmail("bob@example.com");
		request.setNotes("Test");

		this.webTestClient.mutateWith(csrf())
			.put().uri("/animals/1/adoption-requests/2")
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk();

		AdoptionRequest expectedRequest = new AdoptionRequest();
		expectedRequest.setId(2L);
		expectedRequest.setEmail("bob@example.com");
		expectedRequest.setNotes("Test");
		expectedRequest.setAdopterName(TEST_USER_NAME);

		verify(adoptionService).update(eq(1L), refEq(expectedRequest));
	}

	@Test
	@WithMockUser(username = TEST_USER_NAME)
	void shouldDeleteAdoptionRequest() {
		this.webTestClient.mutateWith(csrf())
			.delete().uri("/animals/1/adoption-requests/2")
			.exchange()
			.expectStatus().isOk();

		verify(adoptionService).delete(1L, 2L, TEST_USER_NAME);
	}
}