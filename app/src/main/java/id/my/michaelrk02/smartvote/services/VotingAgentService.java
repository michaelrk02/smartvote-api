package id.my.michaelrk02.smartvote.services;

import bftsmart.tom.MessageContext;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.dao.TokenDao;
import id.my.michaelrk02.smartvote.exceptions.TokenInvalidException;
import id.my.michaelrk02.smartvote.exceptions.TokenUsedException;
import id.my.michaelrk02.smartvote.model.Ballot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

@Service
public class VotingAgentService extends DefaultSingleRecoverable {
    private final Logger logger;
    
    private @Autowired DriverManagerDataSource dataSource;
    private @Autowired TokenDao tokenDao;
    private @Autowired BallotDao ballotDao;
    
    public VotingAgentService() {
        this.logger = LoggerFactory.getLogger(VotingAgentService.class);
    }
    
    public void init(int agentId) {
        if (Boolean.parseBoolean(System.getProperty("agent.init", "false"))) {
            this.logger.info("Executing init SQL");
            try {
                Connection conn = this.dataSource.getConnection();
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/schema.sql"));
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/data.sql"));
            } catch (SQLException ex) {
                this.logger.error("Unable to connect to database");
            }
        }

        this.logger.info("Agent of ID {} has been initialized", agentId);
    }
    
    @Override
    public byte[] appExecuteUnordered(byte[] message, MessageContext ctx) {
        return new byte[0];
    }
    
    @Override
    public byte[] appExecuteOrdered(byte[] message, MessageContext ctx) {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(message);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataInputStream dataIn = new DataInputStream(byteIn);
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        
        try {
            try {
                String messageCode = dataIn.readUTF();
                if (messageCode.equals("VOTE")) {
                    this.executeVoteMessage(ctx, dataIn, dataOut);
                }
            } catch (TokenInvalidException ex) {
                byteOut.reset();
                dataOut.writeUTF("ERROR_TOKEN_INVALID");
            } catch (TokenUsedException ex) {
                byteOut.reset();
                dataOut.writeUTF("ERROR_TOKEN_USED");
            }
        } catch (IOException ex) {
            byteOut.reset();
        }
        
        return byteOut.toByteArray();
    }
    
    @Override
    public void installSnapshot(byte[] snapshot) {
        
    }
    
    private void executeVoteMessage(MessageContext ctx, DataInputStream dataIn, DataOutputStream dataOut) throws IOException, TokenInvalidException, TokenUsedException {
        int token = dataIn.readInt();
        int candidateId = dataIn.readInt();
        int agentId = ctx.getSender();
        
        this.logger.info("Got VOTE({}, {})", token, candidateId);

        if (!this.tokenDao.exists(token)) {
            throw new TokenInvalidException(token);
        }

        if (this.ballotDao.findToken(token).isPresent()) {
            throw new TokenUsedException(token);
        }

        String prevHash = null;
        Optional<Ballot> lastBallot = this.ballotDao.findLast();
        if (lastBallot.isPresent()) {
            prevHash = lastBallot.get().prevHash();
        }

        this.ballotDao.insert(token, candidateId, agentId, prevHash);

        dataOut.writeUTF("SUCCESS");
    }
    
    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }
}
