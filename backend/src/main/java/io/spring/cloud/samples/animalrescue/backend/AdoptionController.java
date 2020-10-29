package io.spring.cloud.samples.animalrescue.backend;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/animals/{animalId}/adoption-requests")
public class AdoptionController {

	private final AdoptionService adoptionService;

	public AdoptionController(AdoptionService adoptionService) {
		this.adoptionService = adoptionService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	void addAdoptionRequest(
		Principal principal,
		@PathVariable Long animalId,
		@RequestBody AdoptionRequest adoptionRequest
	) {
		adoptionRequest.setAdopterName(principal.getName());
		this.adoptionService.add(animalId, adoptionRequest);
	}

	@PutMapping("/{requestId}")
	void updateAdoptionRequest(
		Principal principal,
		@PathVariable Long animalId,
		@PathVariable Long requestId,
		@RequestBody AdoptionRequest adoptionRequest
	) {
		adoptionRequest.setId(requestId);
		adoptionRequest.setAdopterName(principal.getName());
		this.adoptionService.update(animalId, adoptionRequest);
	}

	@DeleteMapping("/{requestId}")
	void deleteAdoptionRequest(
		Principal principal,
		@PathVariable Long animalId,
		@PathVariable Long requestId
	) {
		this.adoptionService.delete(animalId, requestId, principal.getName());
	}
}
