package io.spring.cloud.samples.animalrescue.backend;

import java.util.Objects;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AdoptionService {

	private final AnimalRepository animalRepository;

	public AdoptionService(AnimalRepository animalRepository) {
		this.animalRepository = animalRepository;
	}

	public AdoptionRequest add(Long animalId, AdoptionRequest adoptionRequest) {
		Optional<Animal> animal = this.animalRepository.findById(animalId);
		return animal.map(a ->
		{
			a.getAdoptionRequests().add(adoptionRequest);
			this.animalRepository.save(a);
			return adoptionRequest;
		})
			.orElseThrow(() -> new IllegalArgumentException("Animal with ID " + animalId + " is not found"));
	}

	public AdoptionRequest update(@NonNull Long animalId, AdoptionRequest request) {
		return this.animalRepository.findById(animalId)
			.map(animal -> {
				AdoptionRequest existingRequest = animal.getAdoptionRequests().stream()
					.filter(adoptionRequest -> Objects.equals(request.getId(), adoptionRequest.getId()))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Request with ID " + request.getId() + " is not found"));

				if (!Objects.equals(existingRequest.getAdopterName(), request.getAdopterName())) {
					throw new AccessDeniedException("User " + request.getAdopterName() + " has cannot edit user " + existingRequest.getAdopterName() + "'s adoption request");
				}

				existingRequest.setEmail(request.getEmail());
				existingRequest.setAdopterName(request.getAdopterName());
				existingRequest.setNotes(request.getNotes());

				this.animalRepository.save(animal);

				return existingRequest;
			})
			.orElseThrow(() -> new IllegalArgumentException("Animal with ID " + animalId + " is not found"));
	}

	public void delete(Long animalId, Long requestId, String currentPrincipalName) {
		this.animalRepository.findById(animalId)
			.map(animal -> {
				AdoptionRequest existingRequest = animal.getAdoptionRequests().stream()
					.filter(adoptionRequest -> Objects.equals(requestId, adoptionRequest.getId()))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Request with ID " + requestId + " is not found"));

				if (!Objects.equals(existingRequest.getAdopterName(), currentPrincipalName)) {
					throw new AccessDeniedException("User " + currentPrincipalName + " has cannot delete user " + existingRequest.getAdopterName() + "'s adoption request");
				}

				animal.getAdoptionRequests().remove(existingRequest);
				this.animalRepository.save(animal);
				return existingRequest;
			})
			.orElseThrow(() -> new IllegalArgumentException("Animal with ID " + animalId + " is not found"));
	}
}
