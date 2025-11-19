package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.WorkProject;
import com.syndico.syndicoapp.models.enums.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkProjectRepository extends JpaRepository<WorkProject, Long> {
    List<WorkProject> findByStatus(WorkStatus status);
}
