package id.my.michaelrk02.smartvote.services;

import bftsmart.tom.ServiceProxy;
import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.exceptions.StateInvalidException;
import id.my.michaelrk02.smartvote.exceptions.StateLockedException;
import id.my.michaelrk02.smartvote.exceptions.StateUnlockedException;
import id.my.michaelrk02.smartvote.exceptions.TokenInvalidException;
import id.my.michaelrk02.smartvote.exceptions.TokenUsedException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BroadcastService {
    private @Autowired BallotDao ballotDao;
    
    private final ServiceProxy proxy;
    
    public BroadcastService(ConfigurationService configuration) {
        this.proxy = new ServiceProxy(configuration.agentId);
    }
    
    public void vote(int token, int candidateId) throws IOException, TokenInvalidException, TokenUsedException, StateLockedException, StateInvalidException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        
        dataOut.writeUTF("VOTE");
        dataOut.writeUTF(this.ballotDao.getState());
        dataOut.writeInt(token);
        dataOut.writeInt(candidateId);

        byte[] response = this.proxy.invokeOrdered(byteOut.toByteArray());
        ByteArrayInputStream byteIn = new ByteArrayInputStream(response);
        DataInputStream dataIn = new DataInputStream(byteIn);
        
        String statusCode = dataIn.readUTF();
        if (statusCode.equals("ERROR_TOKEN_INVALID")) {
            token = dataIn.readInt();
            throw new TokenInvalidException(token);
        } else if (statusCode.equals("ERROR_TOKEN_USED")) {
            token = dataIn.readInt();
            throw new TokenUsedException(token);
        } else if (statusCode.equals("ERROR_STATE_LOCKED")) {
            throw new StateLockedException();
        } else if (statusCode.equals("ERROR_STATE_INVALID")) {
            String state = dataIn.readUTF();
            throw new StateInvalidException(state);
        } else if (!statusCode.equals("SUCCESS")) {
            throw new RuntimeException();
        }
    }
    
    public void lock(boolean locked) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        
        dataOut.writeUTF("LOCK");
        dataOut.writeBoolean(locked);

        byte[] response = this.proxy.invokeUnordered(byteOut.toByteArray());
        ByteArrayInputStream byteIn = new ByteArrayInputStream(response);
        DataInputStream dataIn = new DataInputStream(byteIn);
        
        String statusCode = dataIn.readUTF();
        if (!statusCode.equals("SUCCESS")) {
            throw new RuntimeException();
        }
    }
    
    public byte[] recover(String lastState) throws IOException, StateUnlockedException, StateInvalidException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        
        dataOut.writeUTF("RECOVER");
        dataOut.writeUTF(lastState);
        
        byte[] response = this.proxy.invokeUnordered(byteOut.toByteArray());
        ByteArrayInputStream byteIn = new ByteArrayInputStream(response);
        DataInputStream dataIn = new DataInputStream(byteIn);
        
        String statusCode = dataIn.readUTF();
        if (statusCode.equals("ERROR_STATE_UNLOCKED")) {
            throw new StateUnlockedException();
        } else if (statusCode.equals("ERROR_STATE_INVALID")) {
            lastState = dataIn.readUTF();
            throw new StateInvalidException(lastState);
        } else if (!statusCode.equals("SUCCESS")) {
            throw new RuntimeException();
        }
        
        ByteArrayOutputStream stateOut = new ByteArrayOutputStream();
        byteIn.transferTo(stateOut);
        return stateOut.toByteArray();
    }
    
    public String getState() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        
        dataOut.writeUTF("GET_STATE");
        
        byte[] response = this.proxy.invokeUnordered(byteOut.toByteArray());
        ByteArrayInputStream byteIn = new ByteArrayInputStream(response);
        DataInputStream dataIn = new DataInputStream(byteIn);
        
        String statusCode = dataIn.readUTF();
        if (!statusCode.equals("SUCCESS")) {
            throw new RuntimeException();
        }
        
        return dataIn.readUTF();
    }
}
