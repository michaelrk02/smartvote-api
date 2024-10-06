package id.my.michaelrk02.smartvote.handlers;

import bftsmart.tom.MessageContext;
import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.dao.StateDao;
import id.my.michaelrk02.smartvote.dao.TokenDao;
import id.my.michaelrk02.smartvote.interfaces.MessageHandler;
import id.my.michaelrk02.smartvote.model.Ballot;
import id.my.michaelrk02.smartvote.services.ConfigurationService;
import id.my.michaelrk02.smartvote.util.Crypto;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoteHandler implements MessageHandler {    
    private @Autowired ConfigurationService configuration;
    private @Autowired TokenDao tokenDao;
    private @Autowired BallotDao ballotDao;
    private @Autowired StateDao stateDao;
    
    private final Logger logger;
    
    public VoteHandler() {
        this.logger = LoggerFactory.getLogger(VoteHandler.class);
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
        if (this.stateDao.isLocked()) {
            dataOut.writeUTF("ERROR_STATE_LOCKED");
            return;
        }
        
        String globalState = dataIn.readUTF();
        int token = dataIn.readInt();
        int candidateId = dataIn.readInt();
        int agentId = ctx.getSender();
        
        this.logger.info("Got VOTE({}, {}, {})", globalState, token, candidateId);
        
        String localState = this.ballotDao.getState();
        if (!localState.equals(globalState)) {
            dataOut.writeUTF("ERROR_STATE_INVALID");
            dataOut.writeUTF(globalState);
            return;
        }

        if (!this.configuration.agentTokenUnchecked && !this.tokenDao.exists(token)) {
            dataOut.writeUTF("ERROR_TOKEN_INVALID");
            dataOut.writeInt(token);
            return;
        }
        
        if (this.ballotDao.findToken(token).isPresent()) {
            dataOut.writeUTF("ERROR_TOKEN_USED");
            dataOut.writeInt(token);
            return;
        }

        Optional<String> prevHash = Optional.empty();
        Optional<Ballot> lastBallot = this.ballotDao.findLast();
        if (lastBallot.isPresent()) {
            prevHash = Optional.of(lastBallot.get().hash());
        }
        
        if (this.configuration.agentFaulty.equals("receiver")) {
            // malicious ballot
            candidateId = 1;
            this.logger.warn("Malicious ballot casted");
        }

        String hash = Crypto.sha256(String.valueOf(token) + String.valueOf(candidateId) + String.valueOf(agentId) + prevHash.orElse(""));
        this.ballotDao.insert(token, candidateId, agentId, hash, prevHash.orElse(null));

        dataOut.writeUTF("SUCCESS");
    }
}
