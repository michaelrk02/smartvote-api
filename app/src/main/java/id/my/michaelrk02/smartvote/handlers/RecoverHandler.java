package id.my.michaelrk02.smartvote.handlers;

import bftsmart.tom.MessageContext;
import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.dao.StateDao;
import id.my.michaelrk02.smartvote.exceptions.StateInvalidException;
import id.my.michaelrk02.smartvote.interfaces.MessageHandler;
import id.my.michaelrk02.smartvote.model.Ballot;
import id.my.michaelrk02.smartvote.services.recovery.RecoveryMethod;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecoverHandler implements MessageHandler {
    private @Autowired StateDao stateDao;
    private @Autowired BallotDao ballotDao;
    
    private final Logger logger;
    
    public RecoverHandler() {
        this.logger = LoggerFactory.getLogger(RecoverHandler.class);
    }
    
    @Override
    public boolean shouldCompressRequest() {
        return false;
    }
    
    @Override
    public boolean shouldCompressResponse() {
        return false;
    }
    
    @Override
    public void execute(MessageContext ctx, DataInputStream dataIn, DataOutputStream dataOut) throws IOException {
        if (!this.stateDao.isLocked()) {
            dataOut.writeUTF("ERROR_STATE_UNLOCKED");
            return;
        }
        
        String lastState = dataIn.readUTF();
        
        this.logger.debug("Got RECOVER({})", lastState);
        
        RecoveryMethod recoveryMethod;
        List<Ballot> ballots;
        try {
            recoveryMethod = RecoveryMethod.PARTIAL_RECOVERY;
            ballots = this.ballotDao.getData(lastState);
        } catch (StateInvalidException ex) {
            recoveryMethod = RecoveryMethod.FULL_RECOVERY;
            ballots = this.ballotDao.getData();
        }
        
        switch (recoveryMethod) {
            case PARTIAL_RECOVERY -> dataOut.writeUTF("PARTIAL_RECOVERY");
            case FULL_RECOVERY -> dataOut.writeUTF("FULL_RECOVERY");
            default -> throw new RuntimeException();
        }
        
        dataOut.writeUTF(this.ballotDao.getState());
        dataOut.writeInt(ballots.size());

        for (Ballot ballot : ballots) {
            dataOut.writeInt(ballot.token());
            dataOut.writeInt(ballot.candidateId());
            dataOut.writeInt(ballot.agentId());
            dataOut.writeUTF(ballot.hash());
            dataOut.writeUTF(ballot.prevHash() != null ? ballot.prevHash() : "NULL");
        }
    }
}
