package com.example.charity_collection.repository;

import com.example.charity_collection.model.CollectionBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollectionBoxRepository extends JpaRepository<CollectionBox, Long> {

    Optional<CollectionBox> findByIdentifier(String identifier);
}
