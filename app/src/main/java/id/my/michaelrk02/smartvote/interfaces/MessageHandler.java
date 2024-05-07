package id.my.michaelrk02.smartvote.interfaces;

import bftsmart.tom.MessageContext;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface MessageHandler {
    public boolean shouldCompressRequest();
    public boolean shouldCompressResponse();
    public void execute(MessageContext ctx, DataInputStream dataIn, DataOutputStream dataOut) throws IOException;
}
