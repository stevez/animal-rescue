package io.spring.cloud.samples.animalrescue.backend;

import java.time.Duration;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.spring.cloud.samples.animalrescue.backend.fixtures.AdoptionCenterFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdoptionServiceTest {

	private static final int PORT = SocketUtils.findAvailableTcpPort();

	@RegisterExtension
	final AdoptionCenterFixtures fixtures = new AdoptionCenterFixtures(PORT);

	@Autowired
	private AdoptionService adoptionService;

	@DynamicPropertySource
	private static void dependencyProperties(DynamicPropertyRegistry registry) {
		registry.add("adoption-center.uri", () -> "http://localhost:" + PORT);
	}

	@Test
	void addsAdoptionRequest() throws JsonProcessingException {
		AdoptionRequest request = getTestRequest();
		fixtures.stubAddAdoptionWithDefaultResponse();

		AdoptionRequest response = this.adoptionService.add(1L, request).block(Duration.ofMinutes(1));

		assertThat(response).isEqualTo(fixtures.getDefaultResponse());
		fixtures.verifyAddAdoptionCalledWithBody(request, a -> a.setAnimalId(1L));
	}

	@Test
	void shouldThrowExceptionWhenNoAnimalFoundOnAdd() {
		fixtures.stubAddAdoptionWith400Response();
		StepVerifier.create(adoptionService.add(1L, getTestRequest()))
		            .expectError(IllegalArgumentException.class)
		            .verify();
	}

	@Test
	void deletesAdoptionRequest() {
		AdoptionRequest request = getTestRequest();

		this.adoptionService.delete(1L, request.getId(), request.getAdopterName())
		                                               .block(Duration.ofMinutes(1));

		fixtures.verifyDeleteAdoptionCalledWith(1L, request.getId(), request.getAdopterName());
	}

	@Test
	void sampleStepVerifierTestOnTiming() {
		StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofHours(20))
		                                       .thenReturn("hello"))
		            .expectSubscription()
		            .expectNoEvent(Duration.ofHours(10))
		            .thenAwait(Duration.ofHours(10))
		            .expectNext("hello")
		            .expectComplete()
		            .verify();
	}

	private AdoptionRequest getTestRequest() {
		AdoptionRequest request = new AdoptionRequest();
		request.setId(999L);
		request.setEmail("test@example.com");
		request.setAdopterName("Alice");
		request.setNotes("test notes");
		return request;
	}
}
