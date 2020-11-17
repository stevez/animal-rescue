package io.spring.cloud.samples.animalrescue.backend;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AnimalRepository extends PagingAndSortingRepository<Animal, Long> {
}
