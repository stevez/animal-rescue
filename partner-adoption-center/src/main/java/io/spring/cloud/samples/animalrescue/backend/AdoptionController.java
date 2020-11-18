package io.spring.cloud.samples.animalrescue.backend;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adoption-requests")
public class AdoptionController {

	private final AdoptionRequestRepository adoptionRequestRepository;

	public AdoptionController(AdoptionRequestRepository adoptionRequestRepository) {
		this.adoptionRequestRepository = adoptionRequestRepository;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	AdoptionRequest addAdoptionRequest(@RequestBody AdoptionRequest adoptionRequest) {
		return adoptionRequestRepository.save(adoptionRequest);
	}

	@PutMapping("/{requestId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	AdoptionRequest updateAdoptionRequest(@PathVariable Long requestId, @RequestBody AdoptionRequest adoptionRequest) {
		AdoptionRequest existing = adoptionRequestRepository
			.findById(requestId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("AdoptionRequest with id %s is not found.", adoptionRequest.getId())));

		if (!existing.getAdopterName().equals(adoptionRequest.getAdopterName())) {
			throw new IllegalArgumentException(String.format("User %s cannot delete AdoptionRequest with id %s", adoptionRequest.getAdopterName(), adoptionRequest.getId()));
		}
		if (!existing.getAnimalId().equals(adoptionRequest.getAnimalId())) {
			throw new IllegalArgumentException("AdoptionRequest cannot be updated to another animal");
		}

		adoptionRequest.setId(requestId);
		return adoptionRequestRepository.save(adoptionRequest);
	}

	@DeleteMapping("/{requestId}")
	void deleteAdoptionRequest(@PathVariable Long requestId,
	                           @RequestParam("adopter") String adopter,
	                           @RequestParam("animalId") Long animalId) {
		AdoptionRequest existing = adoptionRequestRepository
			.findById(requestId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("AdoptionRequest with id %s is not found.", requestId)));

		if (!existing.getAdopterName().equals(adopter)) {
			throw new IllegalArgumentException(String.format("User %s cannot delete AdoptionRequest with id %s", adopter, requestId));
		}

		if (!existing.getAnimalId().equals(animalId)) {
			throw new IllegalArgumentException("AdoptionRequest cannot be deleted with mismatching animalId");
		}

		adoptionRequestRepository.deleteById(requestId);
	}

	@ExceptionHandler(value = IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	String exceptionHandler(IllegalArgumentException e) {
		return e.getMessage();
	}
}
