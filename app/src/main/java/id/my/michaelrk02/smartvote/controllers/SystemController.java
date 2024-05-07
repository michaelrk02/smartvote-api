package id.my.michaelrk02.smartvote.controllers;

import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.dao.StateDao;
import id.my.michaelrk02.smartvote.exceptions.StateUnlockedException;
import id.my.michaelrk02.smartvote.model.request.LockRequest;
import id.my.michaelrk02.smartvote.model.response.ErrorResponse;
import id.my.michaelrk02.smartvote.model.response.LockResponse;
import id.my.michaelrk02.smartvote.model.response.SyncResponse;
import id.my.michaelrk02.smartvote.services.BroadcastService;
import id.my.michaelrk02.smartvote.services.SynchronizationService;
import java.io.IOException;
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
}
