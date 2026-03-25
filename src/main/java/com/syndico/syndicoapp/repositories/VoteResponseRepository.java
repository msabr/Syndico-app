package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.VoteResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {
    List<VoteResponse> findByVoteId(Long voteId);
    List<VoteResponse> findByResidentId(Long residentId);
    boolean existsByVoteIdAndResidentId(Long voteId, Long residentId);
}
