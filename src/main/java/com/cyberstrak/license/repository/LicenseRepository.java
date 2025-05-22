package com.cyberstrak.license.repository;

import com.cyberstrak.license.entity.License;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for accessing License entities using Spring Data JPA. */
@Repository
public interface LicenseRepository extends JpaRepository<License, String> {
  Optional<License> findByKeyAndProductId(String key, String productId);

  Optional<License> findByKey(String key);
}
