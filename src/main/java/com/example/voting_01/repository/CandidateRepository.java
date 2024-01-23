package com.example.voting_01.repository;

import com.example.voting_01.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Modifying
    @Query("Update Candidate c set c.voteCount = c.voteCount+1 where c.id = ?1 and c.voteCount = ?2")
    int incrementVotes(Long id, Long votes);
}
