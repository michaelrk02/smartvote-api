package id.my.michaelrk02.smartvote.handlers;

import bftsmart.tom.MessageContext;
import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.interfaces.MessageHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetStateHandler implements MessageHandler {
    private @Autowired BallotDao ballotDao;
    
    private final Logger logger;
    
    public GetStateHandler() {
        this.logger = LoggerFactory.getLogger(GetStateHandler.class);
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
        dataOut.writeUTF("SUCCESS");
        dataOut.writeUTF(this.ballotDao.getState());
    }
}
