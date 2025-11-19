package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Vote;
import com.syndico.syndicoapp.models.enums.VoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByStatus(VoteStatus status);
}
