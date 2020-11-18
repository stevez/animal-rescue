package io.spring.cloud.samples.animalrescue.backend;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class AdoptionService {

	private final AnimalRepository animalRepository;
	private final WebClient webClient;

	public AdoptionService(AnimalRepository animalRepository, @Value("${adoption-center.uri}") String adoptionCenterUri) {
		this.animalRepository = animalRepository;
		this.webClient = WebClient.builder()
		                          .baseUrl(adoptionCenterUri)
		                          .build();
	}

	public Mono<AdoptionRequest> add(Long animalId, AdoptionRequest adoptionRequest) {
		return this.animalRepository
			.findById(animalId)
			.map(animal -> {
				adoptionRequest.setAnimalId(animalId);
				return webClient.post()
				                .uri("/adoption-requests")
				                .body(BodyInserters.fromValue(adoptionRequest))
				                .retrieve()
				                .bodyToMono(AdoptionRequest.class)
				                .onErrorResume(
					                e -> e instanceof WebClientResponseException &&
						                HttpStatus.BAD_REQUEST.equals(((WebClientResponseException) e).getStatusCode()),
					                e -> Mono.error(new IllegalArgumentException(e)));
			})
			.orElse(Mono.error(new IllegalArgumentException("Animal with ID " + animalId + " is not found")));
	}

	public Mono<AdoptionRequest> update(Long animalId, AdoptionRequest adoptionRequest) {
		adoptionRequest.setAnimalId(animalId);
		return webClient.put()
		                .uri("/adoption-requests/" + adoptionRequest.getId())
		                .body(BodyInserters.fromValue(adoptionRequest))
		                .retrieve()
		                .bodyToMono(AdoptionRequest.class);
	}

	public Mono<Void> delete(Long animalId, Long requestId, String currentPrincipalName) {
		return webClient.delete()
		                .uri(uriBuilder -> uriBuilder.pathSegment("adoption-requests", Long.toString(requestId))
		                                             .queryParam("adopter", currentPrincipalName)
		                                             .queryParam("animalId", animalId)
		                                             .build())
		                .exchange()
		                .then();
	}
}
