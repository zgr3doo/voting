package com.example.voting_01.repository;

import com.example.voting_01.model.Voter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VoterRepository extends JpaRepository<Voter, Long> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("Select v from Voter v where v.id = ?1")
//    Optional<Voter> findByIdPessimistic(Long id);
}
