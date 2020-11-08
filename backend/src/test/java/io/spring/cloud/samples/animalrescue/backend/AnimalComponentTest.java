package io.spring.cloud.samples.animalrescue.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = {
//		"spring.datasource.initialization-mode=always",
//		"spring.datasource.url=jdbc:tc:postgresql:10.15:///databasename"
	})
@AutoConfigureWebTestClient
class AnimalComponentTest {

//	public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:10:15"));
//
//	@DynamicPropertySource
//	static void redisProperties(DynamicPropertyRegistry registry) {
//		postgres.start();
//		registry.add("spring.datasource.url", postgres::getJdbcUrl);
//		registry.add("spring.datasource.username", postgres::getUsername);
//		registry.add("spring.datasource.password", postgres::getPassword);
//	}

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private AnimalRepository animalRepository;

	@Autowired
	private AdoptionRequestRepository adoptionRequestRepository;

	private long currentAdoptionRequestCountForAnimalId1;

	@BeforeEach
	void setUp() {
		currentAdoptionRequestCountForAnimalId1 = getAdoptionRequestCountForAnimalId1();
	}

	private int getAdoptionRequestCountForAnimalId1() {
		return animalRepository.findById(1L).get().getAdoptionRequests().size();
	}

	@Test
	void getUserName() {
		webTestClient.mutateWith(mockUser("test-user"))
			.get()
			.uri("/whoami")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.isEqualTo("test-user");
	}

	@Test
	void getAllAnimals() {
		webTestClient
			.get()
			.uri("/animals")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.length()").isEqualTo(9)
			.jsonPath("$[0].id").isEqualTo(1)
			.jsonPath("$[0].name").isEqualTo("Chocobo")
			.jsonPath("$[0].avatarUrl").isNotEmpty()
			.jsonPath("$[0].description").isNotEmpty()
			.jsonPath("$[0].rescueDate").isNotEmpty()
			.jsonPath("$[0].adoptionRequests.length()").isEqualTo(currentAdoptionRequestCountForAnimalId1)
			.jsonPath("$[0].adoptionRequests[0].adopterName").isNotEmpty()
			.jsonPath("$[0].adoptionRequests[0].email").isNotEmpty()
			.jsonPath("$[0].adoptionRequests[0].notes").isNotEmpty();
	}

	@Nested
	class SubmitAdoptionRequest {

		@BeforeEach
		void setUp() {
			webTestClient = webTestClient.mutateWith(mockUser("test-user-1"));
		}

		@Test
		void succeeds() {
			String testEmail = "a@email.com";
			String testNotes = "Yaaas!";

			adopt(testEmail, testNotes);

			webTestClient
				.get()
				.uri("/animals")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(9)
				.jsonPath("$[0].id").isEqualTo(1)
				.jsonPath("$[0].name").isEqualTo("Chocobo")
				.jsonPath("$[0].adoptionRequests.length()").isEqualTo(currentAdoptionRequestCountForAnimalId1 + 1)
				.jsonPath("$[0].adoptionRequests[*].adopterName").value(hasItem("test-user-1"))
				.jsonPath("$[0].adoptionRequests[*].email").value(hasItem(testEmail))
				.jsonPath("$[0].adoptionRequests[*].notes").value(hasItem(testNotes));
		}

		@Test
		void failsIfAnimalNotFound() {
			String testEmail = "a@email.com";
			String testNotes = "Yaaas!";

			Map<String, String> requestBody = getRequestBody(testEmail, testNotes);

			webTestClient
				.post()
				.uri("/animals/1000/adoption-requests")
				.body(BodyInserters.fromValue(requestBody))
				.exchange()
				.expectStatus().isBadRequest();
		}
	}

	@Nested
	class EditAdoptionRequest {

		@BeforeEach
		void setUp() {
			webTestClient = webTestClient.mutateWith(mockUser("test-user-2"));
		}

		@Test
		void succeeds() {
			String testEmail = "b@email.com";
			String testNotes = "Plzzzz!";

			adopt("dummy", "dummy");
			long newId = getNewlyCreatedRequestId(1L, "test-user-2");

			webTestClient
				.put()
				.uri("/animals/1/adoption-requests/" + newId)
				.body(BodyInserters.fromValue(getRequestBody(testEmail, testNotes)))
				.exchange()
				.expectStatus().isOk();

			Optional<AdoptionRequest> modified = adoptionRequestRepository.findById(newId);
			assertThat(modified).isPresent();
			assertThat(modified.get().getEmail()).isEqualTo(testEmail);
			assertThat(modified.get().getNotes()).isEqualTo(testNotes);
			assertThat(modified.get().getAdopterName()).isEqualTo("test-user-2");
			assertThat(getAdoptionRequestCountForAnimalId1()).isEqualTo(currentAdoptionRequestCountForAnimalId1 + 1);
		}

		@Test
		void failsIfNotTheOriginalRequester() {
			String testEmail = "a@email.com";
			String testNotes = "Yaaas!";

			Map<String, String> requestBody = getRequestBody(testEmail, testNotes);

			webTestClient
				.put()
				.uri("/animals/1/adoption-requests/2")
				.body(BodyInserters.fromValue(requestBody))
				.exchange()
				.expectStatus().isForbidden();
		}

		@Test
		void failsIfAnimalNotFound() {
			String testEmail = "a@email.com";
			String testNotes = "Yaaas!";

			Map<String, String> requestBody = getRequestBody(testEmail, testNotes);

			webTestClient.mutateWith(mockUser("test-user-2"))
				.put()
				.uri("/animals/1000/adoption-requests/2")
				.body(BodyInserters.fromValue(requestBody))
				.exchange()
				.expectStatus().isBadRequest();
		}

		@Test
		void failsIfAdoptionRequestNotFound() {
			String testEmail = "a@email.com";
			String testNotes = "Yaaas!";

			Map<String, String> requestBody = getRequestBody(testEmail, testNotes);

			webTestClient.mutateWith(mockUser("test-user-2"))
				.put()
				.uri("/animals/1/adoption-requests/2000")
				.body(BodyInserters.fromValue(requestBody))
				.exchange()
				.expectStatus().isBadRequest();
		}
	}

	@Nested
	@WithMockUser(username = "test-user-3")
	class DeleteAdoptionRequest {

		@Test
		void succeeds() {
			adopt("dummy", "dummy");
			long newId = getNewlyCreatedRequestId(1L, "test-user-3");

			webTestClient
				.delete()
				.uri("/animals/1/adoption-requests/" + newId)
				.exchange()
				.expectStatus().isOk();

			assertThat(adoptionRequestRepository.findById(newId)).isNotPresent();
			assertThat(getAdoptionRequestCountForAnimalId1()).isEqualTo(currentAdoptionRequestCountForAnimalId1);
		}

		@Test
		void failsIfNotTheOriginalRequester() {
			webTestClient
				.delete()
				.uri("/animals/1/adoption-requests/3")
				.exchange()
				.expectStatus().isForbidden();
		}

		@Test
		void failsIfAnimalNotFound() {
			webTestClient
				.delete()
				.uri("/animals/1000/adoption-requests/3")
				.exchange()
				.expectStatus().isBadRequest();
		}

		@Test
		void failsIfAdoptionRequestNotFound() {
			webTestClient
				.delete()
				.uri("/animals/1/adoption-requests/3000")
				.exchange()
				.expectStatus().isBadRequest();
		}
	}

	private void adopt(String testEmail, String testNotes) {
		Map<String, String> requestBody = getRequestBody(testEmail, testNotes);

		webTestClient
			.post()
			.uri("/animals/1/adoption-requests")
			.body(BodyInserters.fromValue(requestBody))
			.exchange()
			.expectStatus().isCreated();
	}

	private Map<String, String> getRequestBody(String testEmail, String testNotes) {
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("email", testEmail);
		requestBody.put("notes", testNotes);
		return requestBody;
	}

	private long getNewlyCreatedRequestId(long animalId, String adopterName) {
		return animalRepository
			.findById(animalId)
			.get()
			.getAdoptionRequests()
			.stream()
			.filter(adoptionRequest -> adoptionRequest.getAdopterName().equals(adopterName))
			.findAny()
			.get()
			.getId();
	}
}
