package id.my.michaelrk02.smartvote.controllers;

import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.dao.CandidateDao;
import id.my.michaelrk02.smartvote.dao.TokenDao;
import id.my.michaelrk02.smartvote.exceptions.StateInvalidException;
import id.my.michaelrk02.smartvote.exceptions.StateLockedException;
import id.my.michaelrk02.smartvote.exceptions.StateUnlockedException;
import id.my.michaelrk02.smartvote.exceptions.TokenInvalidException;
import id.my.michaelrk02.smartvote.exceptions.TokenUsedException;
import id.my.michaelrk02.smartvote.model.Candidate;
import id.my.michaelrk02.smartvote.model.request.LoginRequest;
import id.my.michaelrk02.smartvote.model.request.VoteRequest;
import id.my.michaelrk02.smartvote.model.response.ValueResponse;
import id.my.michaelrk02.smartvote.model.response.ErrorResponse;
import id.my.michaelrk02.smartvote.services.BroadcastService;
import id.my.michaelrk02.smartvote.services.ConfigurationService;
import id.my.michaelrk02.smartvote.services.SynchronizationService;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class VotingController {
    private @Autowired ConfigurationService configuration;
    private @Autowired BroadcastService broadcast;
    private @Autowired SynchronizationService synchronization;
    private @Autowired TokenDao tokenDao;
    private @Autowired CandidateDao candidateDao;
    private @Autowired BallotDao ballotDao;
    
    private final Logger logger;
    
    public VotingController() {
        this.logger = LoggerFactory.getLogger(VotingController.class);
    }
    
    @PostMapping("/voting/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        boolean isValid = this.tokenDao.exists(request.token());
        
        return new ResponseEntity(new ValueResponse(isValid), HttpStatus.OK);
    }
    
    @GetMapping("/voting/vote")
    public ResponseEntity<Object> checkVote(@RequestParam int token) {
        boolean hasVoted = this.ballotDao.findToken(token).isPresent();
        
        return new ResponseEntity(new ValueResponse(hasVoted), HttpStatus.OK);
    }
    
    @PostMapping("/voting/vote")
    public ResponseEntity<Object> vote(@RequestBody VoteRequest request) {
        var response = new ResponseEntity(HttpStatus.OK);
        
        try {
            try {
                this.broadcast.vote(request.token(), request.candidateId());
            } catch (StateInvalidException ex) {
                if (!this.configuration.agentAutosync) {
                    throw ex;
                }
                
                this.logger.info("Agent is out of sync. Autosyncing agent");
                
                this.broadcast.lock(true);
                try {
                    this.synchronization.sync();
                } catch (StateUnlockedException _ex) {
                    throw new RuntimeException();
                }
                this.broadcast.lock(false);
                
                this.logger.info("Retransmitting vote message");
                
                this.broadcast.vote(request.token(), request.candidateId());
            }
        } catch (TokenInvalidException | TokenUsedException | StateLockedException ex) {
            response = new ResponseEntity(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException | StateInvalidException | RuntimeException ex) {
            response = new ResponseEntity(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return response;
    }
    
    @GetMapping("/voting/candidates")
    public ResponseEntity<Object> candidates() {
        List<Candidate> candidates = this.candidateDao.getData();
        
        return new ResponseEntity(new ValueResponse(candidates), HttpStatus.OK);
    }
}
