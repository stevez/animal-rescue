package io.spring.cloud.samples.animalrescue.backend;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest
class AdoptionControllerTest {

	@MockBean
	private AdoptionRequestRepository adoptionRequestRepository;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void addAdoptionRequest() {
		AdoptionRequest request = getTestAdoptionRequest();

		webTestClient.post()
		             .uri("/adoption-requests")
		             .bodyValue(request)
		             .exchange()
		             .expectStatus().isCreated();

		verify(adoptionRequestRepository).save(refEq(request));
	}

	@Test
	void updateAdoptionRequest() {
		Long id = 1L;
		AdoptionRequest request = getTestAdoptionRequest();
		when(adoptionRequestRepository.findById(any())).thenReturn(Optional.of(getTestAdoptionRequest()));

		webTestClient.put()
		             .uri("/adoption-requests/1")
		             .bodyValue(request)
		             .exchange()
		             .expectStatus().isNoContent();

		request.setId(id);
		verify(adoptionRequestRepository).findById(id);
		verify(adoptionRequestRepository).save(refEq(request));
	}

	@Test
	void updateAdoptionRequestWhenNotFound() {
		Long id = 1L;
		AdoptionRequest request = getTestAdoptionRequest();

		webTestClient.put()
		             .uri("/adoption-requests/1")
		             .bodyValue(request)
		             .exchange()
		             .expectStatus().isBadRequest();

		verify(adoptionRequestRepository).findById(id);
		verify(adoptionRequestRepository, never()).save(any());
	}

	// more tests to cover all cases

	@Test
	void deleteAdoptionRequest() {
	}

	private AdoptionRequest getTestAdoptionRequest() {
		AdoptionRequest request = new AdoptionRequest();
		request.setAdopterName("bob");
		request.setAnimalId(2L);
		request.setEmail("bob@example.com-" + UUID.randomUUID());
		request.setNotes("Test-" + UUID.randomUUID());
		return request;
	}

}
