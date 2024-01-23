package com.example.voting_01.controller;

import com.example.voting_01.dto.CandidateDTO;
import com.example.voting_01.dto.VoterDTO;
import com.example.voting_01.model.Candidate;
import com.example.voting_01.model.Voter;
import com.example.voting_01.service.VotingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MainController {

    private final VotingService votingService;

    public MainController(VotingService voterService) {
        this.votingService = voterService;
    }

    @GetMapping("/voters")
    List<VoterDTO> allVoters() {
        return votingService.listVoters();
    }

    @GetMapping("/candidates")
    List<CandidateDTO> allCandidates() {
        return votingService.listCandidates();
    }

    @PostMapping("/candidates/new/{name}")
    Candidate newCandidate(@PathVariable String name) {
        return votingService.createCandidate(name);
    }

    @PostMapping("/voters/new/{name}")
    Voter newVoter(@PathVariable String name) {
        return votingService.createVoter(name);
    }

    @PutMapping("/vote/{voterId}/for/{candidateId}")
    boolean castVote(@PathVariable Long voterId, @PathVariable Long candidateId) {
        return votingService.castVote(voterId, candidateId);
    }
}
