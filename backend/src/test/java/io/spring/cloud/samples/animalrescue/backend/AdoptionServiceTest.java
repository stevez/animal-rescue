package io.spring.cloud.samples.animalrescue.backend;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJdbcTest
class AdoptionServiceTest {

	private AdoptionService adoptionService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private AnimalRepository animalRepository;

	@BeforeEach
	void setUp() {
		this.adoptionService = new AdoptionService(this.animalRepository);
	}

	@Test
	void shouldAddAdoptionRequest() {
		AdoptionRequest request = getTestRequest();
		AdoptionRequest updatedRequest = this.adoptionService.add(1L, request);

		Map<String, Object> actualRequest = jdbcTemplate
			.queryForMap("select * from adoption_request where id = ?", updatedRequest.getId());

		assertThat(actualRequest.get("email")).isEqualTo("test@example.com");
		assertThat(actualRequest.get("animal")).isEqualTo(1L);
	}

	@Test
	void shouldThrowExceptionWhenNoAnimalFoundOnAdd() {
		AdoptionRequest request = getTestRequest();
		assertThatThrownBy(() -> this.adoptionService.add(999L, request))
			.isInstanceOf(IllegalArgumentException.class)
			.extracting(Throwable::getMessage)
			.isEqualTo("Animal with ID 999 is not found");
	}

	@Test
	void shouldUpdateAdoptionRequest() {
		AdoptionRequest request = getTestRequest();
		request = this.adoptionService.add(1L, request);

		request.setNotes("Updated note");
		request = this.adoptionService.update(1L, request);

		Map<String, Object> actualRequest = jdbcTemplate
			.queryForMap("select * from adoption_request where id = ?", request.getId());

		assertThat(actualRequest.get("notes")).isEqualTo("Updated note");
		assertThat(actualRequest.get("animal")).isEqualTo(1L);
	}

	@Test
	void shouldThrowExceptionWhenNoAnimalFoundOnUpdate() {
		AdoptionRequest request = getTestRequest();
		assertThatThrownBy(() -> this.adoptionService.update(999L, request))
			.isInstanceOf(IllegalArgumentException.class)
			.extracting(Throwable::getMessage)
			.isEqualTo("Animal with ID 999 is not found");
	}

	@Test
	void shouldThrowExceptionWhenNoRequestFoundOnUpdate() {
		AdoptionRequest request = getTestRequest();
		assertThatThrownBy(() -> this.adoptionService.update(1L, request))
			.isInstanceOf(IllegalArgumentException.class)
			.extracting(Throwable::getMessage)
			.isEqualTo("Request with ID 999 is not found");
	}

	@Test
	void shouldThrowExceptionWhenAdopterNameChanged() {
		AdoptionRequest request = getTestRequest();
		this.adoptionService.add(1L, request);
		request.setAdopterName("Bob");

		assertThatThrownBy(() -> this.adoptionService.update(1L, request))
			.isInstanceOf(AccessDeniedException.class)
			.extracting(Throwable::getMessage)
			.isEqualTo("User Bob has cannot edit user Alice's adoption request");
	}

	@Test
	void shouldDeleteRequest() {
		AdoptionRequest request = getTestRequest();
		request = this.adoptionService.add(1L, request);
		this.adoptionService.delete(1L, request.getId(), request.getAdopterName());

		Integer count = jdbcTemplate
			.queryForObject("select count(*) from adoption_request where id = ?", new Object[] {request.getId()}, Integer.class);

		assertThat(count).isZero();
	}

	@Test
	void shouldThrowExceptionWhenNoAnimalFoundOnDelete() {
		assertThatThrownBy(() -> this.adoptionService.delete(999L, 1L, "Alice"))
			.isInstanceOf(IllegalArgumentException.class)
			.extracting(Throwable::getMessage)
			.isEqualTo("Animal with ID 999 is not found");
	}

	@Test
	void shouldThrowExceptionWhenNoRequestFoundOnDelete() {
		assertThatThrownBy(() -> this.adoptionService.delete(1L, 999L, "Alice"))
			.isInstanceOf(IllegalArgumentException.class)
			.extracting(Throwable::getMessage)
			.isEqualTo("Request with ID 999 is not found");
	}

	@Test
	void shouldThrowExceptionWhenNonOwnerIsTryingToDeleteRequest() {
		AdoptionRequest testRequest = this.getTestRequest();
		this.adoptionService.add(1L, testRequest);

		assertThatThrownBy(() -> this.adoptionService.delete(1L, testRequest.getId(), "Bob"))
			.isInstanceOf(AccessDeniedException.class)
			.extracting(Throwable::getMessage)
			.isEqualTo("User Bob has cannot delete user Alice's adoption request");
	}

	private AdoptionRequest getTestRequest() {
		AdoptionRequest request = new AdoptionRequest();
		request.setId(999L);
		request.setEmail("test@example.com");
		request.setAdopterName("Alice");
		return request;
	}
}