package com.example.voting_01.repository;

import com.example.voting_01.model.Candidate;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("Select c from Candidate c where c.id = ?1")
//    Optional<Candidate> findByIdPessimistic(Long id);

//    @Modifying
//    @Query("Update Candidate c set c.votes = c.votes+1 where c.id = ?1 and c.votes = ?2")
//    int incrementVotes(Long id, Long votes);
}
