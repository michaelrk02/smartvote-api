package id.my.michaelrk02.smartvote.controllers;

import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.dao.StateDao;
import id.my.michaelrk02.smartvote.exceptions.StateUnlockedException;
import id.my.michaelrk02.smartvote.model.Ballot;
import id.my.michaelrk02.smartvote.model.request.LockRequest;
import id.my.michaelrk02.smartvote.model.response.ErrorResponse;
import id.my.michaelrk02.smartvote.model.response.IntegrityCheckResponse;
import id.my.michaelrk02.smartvote.model.response.LockResponse;
import id.my.michaelrk02.smartvote.model.response.SyncResponse;
import id.my.michaelrk02.smartvote.services.BroadcastService;
import id.my.michaelrk02.smartvote.services.SynchronizationService;
import id.my.michaelrk02.smartvote.util.Crypto;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {
    private @Autowired BroadcastService broadcast;
    private @Autowired SynchronizationService synchronization;
    private @Autowired BallotDao ballotDao;
    private @Autowired StateDao stateDao;
    
    @GetMapping("/system/lock")
    public ResponseEntity<Object> getLock() {
        boolean locked = this.stateDao.isLocked();
        return new ResponseEntity(new LockResponse(locked), HttpStatus.OK);
    }
    
    @PutMapping("/system/lock")
    public ResponseEntity<Object> putLock(@RequestBody LockRequest request) {
        try {
            this.broadcast.lock(request.locked());
        } catch (IOException ex) {
            return new ResponseEntity(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @GetMapping("/system/sync")
    public ResponseEntity<Object> getSync() {
        String localState = this.ballotDao.getState();
        String remoteState;
        try {
            remoteState = this.broadcast.getState();
        } catch (IOException ex) {
            return new ResponseEntity(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(new SyncResponse(localState, remoteState, localState.equals(remoteState)), HttpStatus.OK);
    }
    
    @PostMapping("/system/sync")
    public ResponseEntity<Object> sync() {
        try {
            this.synchronization.sync();
        } catch (StateUnlockedException ex) {
            return new ResponseEntity(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException ex) {
            return new ResponseEntity(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @GetMapping("/system/integrity/check")
    public ResponseEntity<Object> checkIntegrity() {
        List<Ballot> ballots = this.ballotDao.getData();
        
        String prevHash = null;
        for (Ballot ballot : ballots) {
            String expectedHash = ballot.hash();
            String actualHash = Crypto.sha256(String.valueOf(ballot.token()) + String.valueOf(ballot.candidateId()) + String.valueOf(ballot.agentId()) + ballot.prevHash());
            if (!actualHash.equals(expectedHash)) {
                return new ResponseEntity(new IntegrityCheckResponse(false, "Ballot ID " + ballot.id() + " hash `" + expectedHash + "` differs than actual hash `" + actualHash + "`"), HttpStatus.OK);
            }
            
            boolean linked = true;
            if (ballot.prevHash() != null) {
                if (!ballot.prevHash().equals(prevHash)) {
                    linked = false;
                }
            } else {
                if (prevHash != null) {
                    linked = false;
                }
            }
            if (!linked) {
                return new ResponseEntity(new IntegrityCheckResponse(false, "Ballot ID " + ballot.id() + " previous hash `" + ballot.prevHash() + "` is not linked with previous ballot with hash `" + prevHash + "`"), HttpStatus.OK);
            }
            
            prevHash = ballot.hash();
        }
        
        return new ResponseEntity(new IntegrityCheckResponse(true, "System passes integrity check"), HttpStatus.OK);
    }
    
    @PostMapping("/system/integrity/repair")
    public ResponseEntity<Object> repairIntegrity() {
        try {
            this.synchronization.repair();
        } catch (StateUnlockedException ex) {
            return new ResponseEntity(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
