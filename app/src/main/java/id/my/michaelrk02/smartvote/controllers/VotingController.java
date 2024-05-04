package id.my.michaelrk02.smartvote.controllers;

import id.my.michaelrk02.smartvote.exceptions.TokenInvalidException;
import id.my.michaelrk02.smartvote.exceptions.TokenUsedException;
import id.my.michaelrk02.smartvote.model.request.VoteRequest;
import id.my.michaelrk02.smartvote.model.response.ErrorResponse;
import id.my.michaelrk02.smartvote.services.BroadcastService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VotingController {    
    private @Autowired BroadcastService broadcast;
    
    @PostMapping("/voting/vote")
    public ResponseEntity<Object> vote(@RequestBody VoteRequest request) {
        var response = new ResponseEntity<>(HttpStatus.OK);
        
        try {
            this.broadcast.vote(request.token(), request.candidateId());
        } catch (IOException ex) {
            response = new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (TokenInvalidException | TokenUsedException ex) {
            response = new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
        
        return response;
    }
}
