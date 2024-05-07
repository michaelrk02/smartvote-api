package id.my.michaelrk02.smartvote.services;

import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.exceptions.StateInvalidException;
import id.my.michaelrk02.smartvote.exceptions.StateUnlockedException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SynchronizationService {
    private @Autowired BroadcastService broadcast;
    private @Autowired BallotDao ballotDao;
    
    private final Logger logger;
    
    public SynchronizationService() {
        this.logger = LoggerFactory.getLogger(SynchronizationService.class);
    }
    
    public void sync() throws IOException, StateUnlockedException {
        this.logger.info("Synchronizing voting agent");
        
        String lastState = this.ballotDao.getState();
        boolean refresh = false;

        byte[] state = null;
        try {
            state = this.broadcast.recover(lastState);
        } catch (StateInvalidException ex) {
            this.logger.info("Latest state is diverging or incompatible with incremental recovery. Starting full recovery instead");
            try {
                state = this.broadcast.recover("NULL");
                refresh = true;
            } catch (StateInvalidException _ex) {
                throw new RuntimeException();
            }
        }

        ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
        DataInputStream dataIn = new DataInputStream(byteIn);

        String stateHash = dataIn.readUTF();         
        if (!stateHash.equals(lastState)) {
            this.logger.info("Got new state `{}` to replace old state `{}`", stateHash, lastState);

            if (refresh) {
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
