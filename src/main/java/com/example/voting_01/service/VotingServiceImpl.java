package com.example.voting_01.service;

import com.example.voting_01.dto.CandidateDTO;
import com.example.voting_01.dto.VoterDTO;
import com.example.voting_01.model.Candidate;
import com.example.voting_01.model.Voter;
import com.example.voting_01.repository.CandidateRepository;
import com.example.voting_01.repository.VoterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Service
public class VotingServiceImpl implements VotingService {
    private static final Logger logger = LoggerFactory.getLogger(VotingServiceImpl.class);
    VoterRepository voterRepository;
    CandidateRepository candidateRepository;

    @PersistenceContext
    EntityManager entityManager;

    public VotingServiceImpl(VoterRepository voterRepository, CandidateRepository candidateRepository) {
        this.voterRepository = voterRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public List<VoterDTO> listVoters() {
        return entityManager.createQuery(""" 
                select
                new com.example.voting_01.dto.VoterDTO(vr.id, vr.name, vr.voted)
                from Voter vr
                group by vr.id
        """, VoterDTO.class).getResultList();
    }

    @Override
    public List<CandidateDTO> listCandidates() {
        return entityManager.createQuery(""" 
                select
                new com.example.voting_01.dto.CandidateDTO(c.id, c.name, c.voteCount)
                from Candidate c
                group by c.id
        """, CandidateDTO.class).getResultList();
    }

    @Override
    public Optional<CandidateDTO> getCandidateDtoById(Long candidateId) {
        CandidateDTO result = null;
        try {
            result = entityManager.createQuery(""" 
                select
                new com.example.voting_01.dto.CandidateDTO(c.id, c.name, c.voteCount)
                from Candidate c
                where c.id = :candidateId
                group by c.id
            """, CandidateDTO.class)
                    .setParameter("candidateId",candidateId)
                    .getSingleResult();
        } catch (NoResultException x) {
            logger.warn("Candidate not found");
        }
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<VoterDTO> getVoterDtoById(Long voterId) {
        VoterDTO result = null;
        try {
            result = entityManager.createQuery(""" 
                select
                new com.example.voting_01.dto.VoterDTO(vr.id, vr.name, vr.voted)
                from Voter vr
                where vr.id = :voterId
                group by vr.id
            """, VoterDTO.class)
                    .setParameter("voterId", voterId)
                    .getSingleResult();
        } catch (NoResultException x) {
            logger.warn("Voter not found");
        }
        return Optional.ofNullable(result);
    }

    @Override
    public Voter createVoter(String name) {
        Voter newVoter = new Voter();
        newVoter.setName(name);
        return voterRepository.save(newVoter);
    }

    @Override
    public Candidate createCandidate(String name) {
        Candidate newCandidate = new Candidate();
        newCandidate.setName(name);
        return candidateRepository.save(newCandidate);
    }

    @Override
    @Transactional
    public boolean castVote(Long voterId, Long candidateId) {
        return repeatFailedAttempts(15, () -> castIdempotentVote(voterId, candidateId));
    }

    public boolean repeatFailedAttempts(int counter, Callable<Boolean> action) {
        boolean result = false;
        while (counter > 0 && !result) {
            try {
//                Thread.sleep(100);
                if (counter == 1) {
                    logger.warn("Repeating failed attempt 0 repeats left");
                }
                else {
                    logger.info("Repeating failed attempt");
                }
                result = action.call();
            } catch (Exception x) {
                Thread.currentThread().interrupt();
                logger.error("Exception: {}", x.getMessage());
            }
            counter--;
        }
        return result;
    }

    public boolean castIdempotentVote(Long voterId, Long candidateId) {
        Voter voter = voterRepository.findById(voterId)
                .orElseThrow(() -> new EntityNotFoundException("Voter not found"));
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));
        entityManager.refresh(candidate);
        entityManager.refresh(voter);
        if (!voter.isVoted()) {
            logger.info("Casting a vote for candidate: {}", candidate);
            boolean voted = candidateRepository.incrementVotes(candidate.getId(), candidate.getVoteCount()) == 1;
            voter.setVoted(voted);
            return voted;
        }
        return false;
    }
}
