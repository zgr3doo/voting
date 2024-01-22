package com.example.voting_01.service;

import com.example.voting_01.dto.CandidateDTO;
import com.example.voting_01.dto.VoterDTO;
import com.example.voting_01.model.Candidate;
import com.example.voting_01.model.Vote;
import com.example.voting_01.model.Voter;
import com.example.voting_01.repository.CandidateRepository;
import com.example.voting_01.repository.VoteRepository;
import com.example.voting_01.repository.VoterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VotingServiceImpl implements VotingService {
    private static final Logger logger = LoggerFactory.getLogger(VotingServiceImpl.class);
    VoterRepository voterRepository;
    VoteRepository voteRepository;
    CandidateRepository candidateRepository;

    @PersistenceContext
    EntityManager entityManager;

    public VotingServiceImpl(VoterRepository voterRepository, VoteRepository voteRepository, CandidateRepository candidateRepository) {
        this.voterRepository = voterRepository;
        this.voteRepository = voteRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public List<VoterDTO> listVoters() {
        return entityManager.createQuery(""" 
                select 
                new com.example.voting_01.dto.VoterDTO(vr.id, vr.name, (count(v.candidateId) > 0)) 
                from Voter vr 
                left join Vote v on v.voterId = vr.id
                group by vr.id
        """, VoterDTO.class).getResultList();
    }

    @Override
    public List<CandidateDTO> listCandidates() {
        return entityManager.createQuery(""" 
                select 
                new com.example.voting_01.dto.CandidateDTO(c.id, c.name, count(v.voterId)) 
                from Candidate c
                left join Vote v on v.candidateId = c.id
                group by c.id
        """, CandidateDTO.class).getResultList();
    }

    @Override
    public Optional<CandidateDTO> getCandidateDtoById(Long candidateId) {
        CandidateDTO result = null;
        try {
            result = entityManager.createQuery(""" 
                select 
                new com.example.voting_01.dto.CandidateDTO(c.id, c.name, count(v.voterId)) 
                from Candidate c
                left join Vote v on v.candidateId = c.id
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
                new com.example.voting_01.dto.VoterDTO(vr.id, vr.name, (count(v.candidateId) > 0)) 
                from Voter vr 
                left join Vote v on v.voterId = vr.id
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
    public Vote castVote(Long voterId, Long candidateId) {
        Vote newVote = new Vote();
        newVote.setVoterId(voterId);
        newVote.setCandidateId(candidateId);
        return voteRepository.save(newVote);
    }

//    @Transactional
//    public boolean castVoteIdempotent(Long voterId, Long candidateId) {
//        Voter voter = voterRepository.findById(voterId)
//                .orElseThrow(() -> new EntityNotFoundException("Voter not found"));
//        Candidate candidate = candidateRepository.findById(candidateId)
//                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));
//        if (!voter.isVoted()) {
//            logger.info("Casting a vote for candidate: " + candidate);
//            boolean voted = candidateRepository.incrementVotes(candidate.getId(), candidate.getVotes()) == 1;
//            voter.setVoted(voted);
//            voter.setVoted(true);
//            return true;
//        }
//        return false;
//    }
//
//    @Transactional
//    public boolean castVotePessimistic(Long voterId, Long candidateId) {
//        Voter voter = voterRepository.findByIdPessimistic(voterId)
//                .orElseThrow(() -> new EntityNotFoundException("Voter not found"));
//        Candidate candidate = candidateRepository.findByIdPessimistic(candidateId)
//                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));
//        if (!voter.isVoted()) {
//            logger.info("Casting a vote for candidate: " + candidate);
//            candidate.setVotes(candidate.getVotes() + 1);
//            voter.setVoted(true);
//            return true;
//        }
//        return false;
//    }
}
