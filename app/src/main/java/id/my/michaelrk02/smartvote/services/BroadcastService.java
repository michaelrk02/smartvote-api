package id.my.michaelrk02.smartvote.services;

import bftsmart.tom.ServiceProxy;
import id.my.michaelrk02.smartvote.exceptions.TokenInvalidException;
import id.my.michaelrk02.smartvote.exceptions.TokenUsedException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class BroadcastService {
    private final ServiceProxy proxy;
    
    public BroadcastService() {
        int processId = Integer.parseInt(System.getProperty("agent.id", "0"));
        this.proxy = new ServiceProxy(processId);
    }
    
    public void vote(int token, int candidateId) throws IOException, TokenInvalidException, TokenUsedException
    {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        
        dataOut.writeUTF("VOTE");
        dataOut.writeInt(token);
        dataOut.writeInt(candidateId);

        byte[] response = this.proxy.invokeOrdered(byteOut.toByteArray());
        ByteArrayInputStream byteIn = new ByteArrayInputStream(response);
        DataInputStream dataIn = new DataInputStream(byteIn);
        
        String statusCode = dataIn.readUTF();
        if (statusCode.equals("ERROR_TOKEN_INVALID")) {
            throw new TokenInvalidException(token);
        } else if (statusCode.equals("ERROR_TOKEN_USED")) {
            throw new TokenUsedException(token);
        }
    }
}
