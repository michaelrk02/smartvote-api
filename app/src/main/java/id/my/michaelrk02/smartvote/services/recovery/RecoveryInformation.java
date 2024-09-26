package id.my.michaelrk02.smartvote.services.recovery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RecoveryInformation {
    private RecoveryMethod method;
    private byte[] data;
    
    public RecoveryInformation(String statusCode, ByteArrayInputStream byteIn) throws IOException {
        if (statusCode.equals("PARTIAL_RECOVERY")) {
            this.method = RecoveryMethod.PARTIAL_RECOVERY;
        } else if (statusCode.equals("FULL_RECOVERY")) {
            this.method = RecoveryMethod.FULL_RECOVERY;
        }
        
        ByteArrayOutputStream stateOut = new ByteArrayOutputStream();
        byteIn.transferTo(stateOut);
        this.data = stateOut.toByteArray();
    }
    
    public RecoveryMethod getMethod() {
        return this.method;
    }
    
    public byte[] getData() {
        return this.data;
    }
}
