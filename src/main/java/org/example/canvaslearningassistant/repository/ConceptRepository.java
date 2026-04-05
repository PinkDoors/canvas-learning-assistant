package org.example.canvaslearningassistant.repository;

import org.example.canvaslearningassistant.entity.Concept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConceptRepository extends JpaRepository<Concept, Long> {

    Optional<Concept> findByName(String name);

    List<Concept> findByNameIn(List<String> names);

    boolean existsByName(String name);
}