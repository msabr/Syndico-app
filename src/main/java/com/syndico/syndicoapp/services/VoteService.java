package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Vote;
import com.syndico.syndicoapp.models.VoteResponse;
import com.syndico.syndicoapp.models.enums.VoteStatus;
import com.syndico.syndicoapp.repositories.VoteRepository;
import com.syndico.syndicoapp.repositories.VoteResponseRepository;
import com.syndico.syndicoapp.repositories.AssemblyMeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private VoteResponseRepository voteResponseRepository;

    @Autowired
    private AssemblyMeetingRepository assemblyMeetingRepository;

    // Create vote
    public Vote createVote(Vote vote) {
        if (vote.getStatus() == null) {
            vote.setStatus(VoteStatus.OUVERT);
        }
        return voteRepository.save(vote);
    }

    // Get all votes
    public List<Vote> getAllVotes() {
        return voteRepository.findAll();
    }

    // Get vote by ID
    public Optional<Vote> getVoteById(Long id) {
        return voteRepository.findById(id);
    }

    // Update vote
    public Vote updateVote(Long id, Vote voteDetails) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found with id: " + id));

        vote.setQuestion(voteDetails.getQuestion());
        vote.setOptions(voteDetails.getOptions());
        vote.setStartDate(voteDetails.getStartDate());
        vote.setEndDate(voteDetails.getEndDate());
        vote.setStatus(voteDetails.getStatus());

        return voteRepository.save(vote);
    }

    // Delete vote
    public void deleteVote(Long id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found with id: " + id));
        voteRepository.delete(vote);
    }

    // Get votes by status
    public List<Vote> getVotesByStatus(VoteStatus status) {
        return voteRepository.findByStatus(status);
    }

    // Get active votes (open and current)
    public List<Vote> getActiveVotes() {
        LocalDateTime now = LocalDateTime.now();
        return voteRepository.findAll().stream()
                .filter(v -> v.getStatus() == VoteStatus.OUVERT)
                .filter(v -> v.getStartDate().isBefore(now) || v.getStartDate().isEqual(now))
                .filter(v -> v.getEndDate().isAfter(now))
                .collect(Collectors.toList());
    }

    // Submit vote response
    public VoteResponse submitVote(Long voteId, Long residentId, String selectedOption) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found with id: " + voteId));

        // Check if vote is open
        if (vote.getStatus() != VoteStatus.OUVERT) {
            throw new RuntimeException("This vote is closed");
        }

        // Check if voting period is valid
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(vote.getStartDate()) || now.isAfter(vote.getEndDate())) {
            throw new RuntimeException("Voting period has ended or not started yet");
        }

        // Check if resident already voted
        if (voteResponseRepository.existsByVoteIdAndResidentId(voteId, residentId)) {
            throw new RuntimeException("You have already voted");
        }

        // Check if selected option is valid
        if (!vote.getOptions().contains(selectedOption)) {
            throw new RuntimeException("Invalid vote option");
        }

        VoteResponse voteResponse = new VoteResponse();
        voteResponse.setVote(vote);
        voteResponse.setResident(residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found")));
        voteResponse.setSelectedOption(selectedOption);
        voteResponse.setVotedAt(LocalDateTime.now());

        return voteResponseRepository.save(voteResponse);
    }

    // Get vote results
    public VoteResults getVoteResults(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found with id: " + voteId));

        List<VoteResponse> responses = voteResponseRepository.findByVoteId(voteId);

        Map<String, Long> resultsByOption = new HashMap<>();
        for (String option : vote.getOptions()) {
            long count = responses.stream()
                    .filter(r -> r.getSelectedOption().equals(option))
                    .count();
            resultsByOption.put(option, count);
        }

        long totalVotes = responses.size();

        return new VoteResults(vote, resultsByOption, totalVotes);
    }

    // Close vote
    public Vote closeVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found with id: " + voteId));

        vote.setStatus(VoteStatus.FERME);
        return voteRepository.save(vote);
    }

    // Cancel vote
    public Vote cancelVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found with id: " + voteId));

        vote.setStatus(VoteStatus.ANNULE);
        return voteRepository.save(vote);
    }

    // Check if resident has voted
    public boolean hasResidentVoted(Long voteId, Long residentId) {
        return voteResponseRepository.existsByVoteIdAndResidentId(voteId, residentId);
    }

    // Get vote statistics
    public VoteStatistics getVoteStatistics() {
        List<Vote> allVotes = voteRepository.findAll();

        long totalVotes = allVotes.size();
        long openVotes = allVotes.stream()
                .filter(v -> v.getStatus() == VoteStatus.OUVERT)
                .count();
        long closedVotes = allVotes.stream()
                .filter(v -> v.getStatus() == VoteStatus.FERME)
                .count();
        long cancelledVotes = allVotes.stream()
                .filter(v -> v.getStatus() == VoteStatus.ANNULE)
                .count();

        long totalResponses = voteResponseRepository.count();

        double avgParticipationRate = allVotes.stream()
                .filter(v -> v.getStatus() == VoteStatus.FERME)
                .mapToLong(v -> voteResponseRepository.findByVoteId(v.getId()).size())
                .average()
                .orElse(0.0);

        return new VoteStatistics(totalVotes, openVotes, closedVotes, cancelledVotes,
                totalResponses, avgParticipationRate);
    }

    @Autowired
    private com.syndico.syndicoapp.repositories.ResidentRepository residentRepository;

    // Inner classes
    public static class VoteResults {
        private Vote vote;
        private Map<String, Long> resultsByOption;
        private long totalVotes;

        public VoteResults(Vote vote, Map<String, Long> resultsByOption, long totalVotes) {
            this.vote = vote;
            this.resultsByOption = resultsByOption;
            this.totalVotes = totalVotes;
        }

        public Vote getVote() { return vote; }
        public Map<String, Long> getResultsByOption() { return resultsByOption; }
        public long getTotalVotes() { return totalVotes; }

        public double getPercentage(String option) {
            Long count = resultsByOption.getOrDefault(option, 0L);
            return totalVotes > 0 ? ((double) count / totalVotes) * 100 : 0;
        }
    }

    public static class VoteStatistics {
        private long totalVotes;
        private long openVotes;
        private long closedVotes;
        private long cancelledVotes;
        private long totalResponses;
        private double avgParticipationRate;

        public VoteStatistics(long totalVotes, long openVotes, long closedVotes,
                              long cancelledVotes, long totalResponses, double avgParticipationRate) {
            this.totalVotes = totalVotes;
            this.openVotes = openVotes;
            this.closedVotes = closedVotes;
            this.cancelledVotes = cancelledVotes;
            this.totalResponses = totalResponses;
            this.avgParticipationRate = avgParticipationRate;
        }

        public long getTotalVotes() { return totalVotes; }
        public long getOpenVotes() { return openVotes; }
        public long getClosedVotes() { return closedVotes; }
        public long getCancelledVotes() { return cancelledVotes; }
        public long getTotalResponses() { return totalResponses; }
        public double getAvgParticipationRate() { return avgParticipationRate; }
    }
}
