package id.my.michaelrk02.smartvote.services;

import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.dao.StateDao;
import id.my.michaelrk02.smartvote.exceptions.StateUnlockedException;
import id.my.michaelrk02.smartvote.model.Ballot;
import id.my.michaelrk02.smartvote.services.recovery.RecoveryInformation;
import id.my.michaelrk02.smartvote.services.recovery.RecoveryMethod;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SynchronizationService {
    private @Autowired BroadcastService broadcast;
    private @Autowired BallotDao ballotDao;
    private @Autowired StateDao stateDao;
    
    private final Logger logger;
    
    public SynchronizationService() {
        this.logger = LoggerFactory.getLogger(SynchronizationService.class);
    }
    
    public void repair() throws StateUnlockedException {
        if (!this.stateDao.isLocked()) {
            throw new StateUnlockedException();
        }
        
        this.logger.info("Repairing voting agent state");
        
        List<Ballot> ballots = this.ballotDao.getData();
        
        String prevHash = null;
        for (Ballot ballot : ballots) {
            prevHash = this.ballotDao.rehash(ballot.id(), prevHash);
        }
        
        this.logger.info("Voting agent state repaired");
    }
    
    public void sync() throws IOException, StateUnlockedException {
        this.logger.info("Synchronizing voting agent");
        
        // perform additional repair step first
        this.repair();
        
        String lastState = this.ballotDao.getState();
        
        RecoveryInformation recovery = this.broadcast.recover(lastState);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(recovery.getData());
        DataInputStream dataIn = new DataInputStream(byteIn);

        String stateHash = dataIn.readUTF();    
        if (!stateHash.equals(lastState)) {
            this.logger.info("Got new state `{}` to replace old state `{}`", stateHash, lastState);

            if (recovery.getMethod() == RecoveryMethod.PARTIAL_RECOVERY) {
                this.logger.info("Attempting partial recovery ...");
            } else if (recovery.getMethod() == RecoveryMethod.FULL_RECOVERY) {
                this.logger.info("Attempting full recovery ...");
                this.ballotDao.clear();
            }

            int ballotCount = dataIn.readInt();
            for (int i = 0; i < ballotCount; i++) {
                int token = dataIn.readInt();
                int candidateId = dataIn.readInt();
                int agentId = dataIn.readInt();
                String hash = dataIn.readUTF();
                String prevHash = dataIn.readUTF();

                this.ballotDao.insert(token, candidateId, agentId, hash, !prevHash.equals("NULL") ? prevHash : null);
            }

            this.logger.info("Successfully restored {} ballots", ballotCount);
        } else {
            this.logger.info("New state is the same as the old state, skipping state transfer");
        }
        
        this.logger.info("Synchronization finished");
    }
}
