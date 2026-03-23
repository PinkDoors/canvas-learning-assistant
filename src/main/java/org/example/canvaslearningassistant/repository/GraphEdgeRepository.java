package org.example.canvaslearningassistant.repository;

import org.example.canvaslearningassistant.entities.GraphEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GraphEdgeRepository extends JpaRepository<GraphEdge, Long> {
    List<GraphEdge> findByCourseId(Integer courseId);
}