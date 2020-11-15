package io.spring.cloud.samples.animalrescue.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcOperations;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
class AnimalRepositoryTest {

	@Autowired
	AnimalRepository animalRepository;

	@Autowired
	JdbcOperations jdbcOperations;

	@Test
	void shouldReturnAnimalsWithNoAdoptionRequests() {
		Long testAnimalWithRequests = 2L;
		Long testAnimalWithNoRequests = 3L;
		this.jdbcOperations.update("delete from adoption_request where animal = ?", testAnimalWithNoRequests);

		Iterable<Animal> animals = animalRepository.findAnimalsWithNoAdoptionRequests();
		assertThat(animals).extracting(Animal::getId)
			.contains(testAnimalWithNoRequests)
			.doesNotContain(testAnimalWithRequests);
	}
}