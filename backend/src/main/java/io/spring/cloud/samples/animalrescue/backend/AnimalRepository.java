package io.spring.cloud.samples.animalrescue.backend;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface AnimalRepository extends CrudRepository<Animal, Long> {

	@Query("select * from ANIMAL where (select count(id) from ADOPTION_REQUEST where ANIMAL = ANIMAL.ID) = 0")
	Iterable<Animal> findAnimalsWithNoAdoptionRequests();
}
