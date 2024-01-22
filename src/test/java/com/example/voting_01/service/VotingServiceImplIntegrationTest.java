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
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class VotingServiceImplIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(VotingServiceImplIntegrationTest.class);
    @Autowired
    VoterRepository voterRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    CandidateRepository candidateRepository;
    @Autowired
    VotingServiceImpl sut;
    @PersistenceContext
    EntityManager em;

    @Test
    void listVoters() {
        // given
        Voter voter1 = createVoter("Spiderman", voterRepository);
        VoterDTO expected = new VoterDTO(voter1.getId(), voter1.getName(), false);
        // when
        List<VoterDTO> actual = sut.listVoters();
        // then
        Assertions.assertEquals(expected, actual.get(0));
    }

    @Test
    void listCandidates() {
        // given
        Candidate candidate = createCandidate("Jesse James", candidateRepository);
        CandidateDTO expected = new CandidateDTO(candidate.getId(), candidate.getName(), 0L);
        // when
        List<CandidateDTO> actual = sut.listCandidates();
        // then
        Assertions.assertEquals(expected, actual.get(0));
    }

    @Test
    void createVoterTest() {
        // given
        String expectedName = "Batman";
        boolean expectedVotedState = false;
        // when
        Voter voter = sut.createVoter(expectedName);
        // then
        VoterDTO actual = sut.getVoterDtoById(voter.getId())
                .orElseThrow(() -> new EntityNotFoundException("Voter not found"));
        Assertions.assertEquals(expectedName, actual.getName());
        Assertions.assertEquals(expectedVotedState, actual.isVoted());
    }

    @Test
    void createCandidateTest() {
        // given
        String expectedName = "Donald Duck";
        long expectedVotesCount = 0L;
        // when
        Candidate candidate = sut.createCandidate(expectedName);
        // then
        CandidateDTO actual = sut.getCandidateDtoById(candidate.getId())
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));
        Assertions.assertEquals(expectedName, actual.getName());
        Assertions.assertEquals(expectedVotesCount, actual.getVotes());
    }

    @Test
    void castVoteTest() {
        // given
        Voter voter = createVoter("Spiderman", voterRepository);
        Candidate candidate = createCandidate("Mickey Mouse", candidateRepository);
        // when
        Vote actual = sut.castVote(voter.getId(), candidate.getId());

        // then
        VoterDTO voterDTO = sut.getVoterDtoById(voter.getId())
                .orElseThrow(() -> new EntityNotFoundException("Voter not found"));
        CandidateDTO candidateDTO = sut.getCandidateDtoById(candidate.getId())
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        Assertions.assertEquals(voter.getId(), actual.getVoterId());
        Assertions.assertEquals(candidate.getId(), actual.getCandidateId());
        Assertions.assertEquals(1L, candidateDTO.getVotes());
        Assertions.assertTrue(voterDTO.isVoted());
    }

    @Test
    void castVoteCollisionTest() throws InterruptedException {
        // given
        final int threadCount = 2;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        Voter voter1 = createVoter("Spiderman", voterRepository);
        Voter voter2 = createVoter("Ant-man", voterRepository);
        Candidate candidate = createCandidate("Mickey Mouse", candidateRepository);
        List<Task> todo = new ArrayList<>();
        todo.add(new Task(readyLatch, () -> sut.castVote(voter1.getId(), candidate.getId())));
        todo.add(new Task(readyLatch, () -> sut.castVote(voter2.getId(), candidate.getId())));
        ExecutorService es = Executors.newFixedThreadPool(threadCount);

        // when
        todo.forEach(es::submit);
        es.shutdown();
        try{
            es.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException x) {
            logger.info("Exception: {}",x.getMessage());
        }

        // then
        VoterDTO voterDTO1 = sut.getVoterDtoById(voter1.getId())
                .orElseThrow(() -> new EntityNotFoundException("Voter not found"));
        VoterDTO voterDTO2 = sut.getVoterDtoById(voter1.getId())
                .orElseThrow(() -> new EntityNotFoundException("Voter not found"));
        CandidateDTO candidateDTO = sut.getCandidateDtoById(candidate.getId())
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        Assertions.assertEquals(2L, candidateDTO.getVotes());
        Assertions.assertTrue(voterDTO1.isVoted());
        Assertions.assertTrue(voterDTO2.isVoted());
    }

    @Test
    void castVoteDuplicateCollisionTest() throws InterruptedException {
        // given
        final int threadCount = 2;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        Voter voter = createVoter("Spiderman", voterRepository);
        Candidate candidate = createCandidate("Mickey Mouse", candidateRepository);
        List<Task> todo = new ArrayList<>();
        todo.add(new Task(readyLatch, () -> sut.castVote(voter.getId(), candidate.getId())));
        todo.add(new Task(readyLatch, () -> sut.castVote(voter.getId(), candidate.getId())));
        ExecutorService es = Executors.newFixedThreadPool(threadCount);

        // when
        todo.forEach(es::submit);
        es.shutdown();

        try{
            es.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException x) {
            logger.info("Exception: {}",x.getMessage());
        }
        logger.info("Done");

        // then
        VoterDTO voterDTO = sut.getVoterDtoById(voter.getId())
                .orElseThrow(() -> new EntityNotFoundException("Voter not found"));
        CandidateDTO candidateDTO = sut.getCandidateDtoById(candidate.getId())
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        Assertions.assertEquals(1L, candidateDTO.getVotes());
        Assertions.assertTrue(voterDTO.isVoted());
    }

    @Test
    void castVoteWaitAndCatchFireTest() {
        // given
        final int threadCount = 200;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        Candidate candidate = createCandidate("Mickey Mouse", candidateRepository);
        Runnable action = () -> {
            Voter voter = createVoter("Borg" + System.nanoTime(), voterRepository);
            logger.info("Created Voter: {}", voter);
            sut.castVote(voter.getId(), candidate.getId());
        };
        List<Task> todo = IntStream.range(0,threadCount).boxed()
                .map(i -> new Task(readyLatch, action))
                .toList();
        ExecutorService es = Executors.newFixedThreadPool(threadCount);

        // when
        todo.forEach(es::submit);
        es.shutdown();

        try{
            es.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException x) {
            logger.info("Exception: {}",x.getMessage());
        }
        logger.info("Done");

        // then
        CandidateDTO candidateDTO = sut.getCandidateDtoById(candidate.getId())
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));
        Assertions.assertEquals(threadCount, candidateDTO.getVotes());
    }

    private Candidate createCandidate(String name, CandidateRepository repository) {
        Candidate candidate = new Candidate();
        candidate.setName(name);
        return repository.save(candidate);
    }

    private Voter createVoter(String name, VoterRepository repository) {
        Voter voter = new Voter();
        voter.setName(name);
        return repository.save(voter);
    }

    static class Task implements Runnable {
        CountDownLatch ready;
        Runnable execution;

        public Task(CountDownLatch ready, Runnable execution) {
            this.ready = ready;
            this.execution = execution;
        }

        @Override
        public void run() {
            try {
                ready.countDown();
                logger.info("Waiting: {}",Thread.currentThread().getName());
                ready.await();
                execution.run();
            } catch (Exception e) {
                logger.info("Exception: {}",e.getMessage());
            }
        }
    }
}