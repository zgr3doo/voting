package com.example.voting_01.service;

import com.example.voting_01.dto.CandidateDTO;
import com.example.voting_01.dto.VoterDTO;
import com.example.voting_01.model.Candidate;
import com.example.voting_01.model.Vote;
import com.example.voting_01.model.Voter;

import java.util.List;
import java.util.Optional;

public interface VotingService {
    List<VoterDTO> listVoters();

    List<CandidateDTO> listCandidates();

    Optional<CandidateDTO> getCandidateDtoById(Long candidateId);

    Optional<VoterDTO> getVoterDtoById(Long voterId);

    Voter createVoter(String name);

    Candidate createCandidate(String name);

    Vote castVote(Long voterId, Long candidateId);
}
