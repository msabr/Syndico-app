package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.TimelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineStageRepository extends JpaRepository<TimelineStage, Long> {

    List<TimelineStage> findByWorkProjectId(Long workProjectId);
    List<TimelineStage> findByWorkProjectIdOrderByStartDateAsc(Long workProjectId);
}
