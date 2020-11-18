package io.spring.cloud.samples.animalrescue.backend;

import org.springframework.data.annotation.Id;

public class AdoptionRequest {
	@Id
	private Long id;

	private Long animalId;

	private String adopterName;

	private String email;

	private String notes;

	@java.lang.Override
	public java.lang.String toString() {
		return "AdoptionRequest{" +
			"id=" + id +
			", animalId=" + animalId +
			", adopterName='" + adopterName + '\'' +
			", email='" + email + '\'' +
			", notes='" + notes + '\'' +
			'}';
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAnimalId() {
		return animalId;
	}

	public void setAnimalId(Long animalId) {
		this.animalId = animalId;
	}

	public String getAdopterName() {
		return adopterName;
	}

	public void setAdopterName(String adopterName) {
		this.adopterName = adopterName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
