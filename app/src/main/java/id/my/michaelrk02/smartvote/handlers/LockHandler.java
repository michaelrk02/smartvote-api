package id.my.michaelrk02.smartvote.handlers;

import bftsmart.tom.MessageContext;
import id.my.michaelrk02.smartvote.dao.StateDao;
import id.my.michaelrk02.smartvote.interfaces.MessageHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LockHandler implements MessageHandler {
    private @Autowired StateDao stateDao;
    
    private final Logger logger;
    
    public LockHandler() {
        this.logger = LoggerFactory.getLogger(LockHandler.class);
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
        boolean locked = dataIn.readBoolean();
        
        this.logger.info("Got LOCK({})", locked);
        this.stateDao.setLocked(locked);
        
        dataOut.writeUTF("SUCCESS");
    }
}
