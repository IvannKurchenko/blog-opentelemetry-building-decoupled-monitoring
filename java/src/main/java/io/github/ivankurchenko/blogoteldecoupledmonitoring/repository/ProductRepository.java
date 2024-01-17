package io.github.ivankurchenko.blogoteldecoupledmonitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
