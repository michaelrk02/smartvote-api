package id.my.michaelrk02.smartvote.services;

import bftsmart.tom.MessageContext;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import id.my.michaelrk02.smartvote.dao.BallotDao;
import id.my.michaelrk02.smartvote.handlers.GetStateHandler;
import id.my.michaelrk02.smartvote.handlers.LockHandler;
import id.my.michaelrk02.smartvote.handlers.RecoverHandler;
import id.my.michaelrk02.smartvote.handlers.VoteHandler;
import id.my.michaelrk02.smartvote.interfaces.MessageHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

@Service
public class VotingAgentService extends DefaultSingleRecoverable {
    private @Autowired BallotDao ballotDao;
    
    private final Logger logger;
    private final Map<String, MessageHandler> messageHandlers;
    
    public VotingAgentService(
            DriverManagerDataSource dataSource,
            ConfigurationService configuration,
            VoteHandler voteHandler,
            LockHandler lockHandler,
            RecoverHandler recoverHandler,
            GetStateHandler getStateHandler
    ) {
        this.logger = LoggerFactory.getLogger(VotingAgentService.class);
        this.messageHandlers = new HashMap<>();
        this.logger.info("Initializing agent");
        
        this.logger.info("Initializing SQL database state");
        while (true) {
            try {
                this.initializeDatabase(dataSource, configuration);
                break;
            } catch (SQLException ex) {
                this.logger.error("Unable to connect to database, retrying ...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException _ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        this.messageHandlers.put("VOTE", voteHandler);
        this.messageHandlers.put("LOCK", lockHandler);
        this.messageHandlers.put("RECOVER", recoverHandler);
        this.messageHandlers.put("GET_STATE", getStateHandler);
        
        if (!configuration.agentFaulty.equals("")) {
            this.logger.warn("Agent of ID {} is a malicious {}", configuration.agentId, configuration.agentFaulty);
        }

        this.logger.info("Agent of ID {} initialized", configuration.agentId);
    }
    
    @Override
    public byte[] appExecuteUnordered(byte[] message, MessageContext ctx) {
        return this.executeMessage(message, ctx);
    }
    
    @Override
    public byte[] appExecuteOrdered(byte[] message, MessageContext ctx) {
        return this.executeMessage(message, ctx);
    }
    
    @Override
    public void installSnapshot(byte[] snapshot) {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(snapshot);
        DataInputStream dataIn = new DataInputStream(byteIn);
        
        try {
            String currentState = this.ballotDao.getState();
            String latestState = dataIn.readUTF();
            
            if (!currentState.equals(latestState)) {
                this.logger.warn("System state is outdated (current: {}, latest: {}). Please start recovery as soon as possible", currentState, latestState);
            } else {
                this.logger.info("System state is synchronized with hash `{}`", latestState);
            }
        } catch (IOException ex) {
            this.logger.error("Unable to get latest state information");
        }
    }
    
    @Override
    public byte[] getSnapshot() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        
        try {
            dataOut.writeUTF(this.ballotDao.getState());
        } catch (IOException ex) {
        }
        
        return byteOut.toByteArray();
    }
    
    private byte[] executeMessage(byte[] message, MessageContext ctx) {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(message);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataInputStream dataIn = new DataInputStream(byteIn);
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        
        try {            
            String messageCode = dataIn.readUTF();
            MessageHandler handler = this.messageHandlers.get(messageCode);
            if (handler != null) {
                handler.execute(ctx, dataIn, dataOut);
                
                this.logger.info("Message {} ({} bytes) executed successfully. Sent {} bytes to the response", messageCode, message.length, byteOut.size());
            } else {
                dataOut.writeUTF("ERROR_MESSAGE_INVALID");
                dataOut.writeUTF(messageCode);
                
                this.logger.info("Invalid message: {}", messageCode);
            }
        } catch (IOException ex) {
            byteOut.reset();
        }
        
        return byteOut.toByteArray();
    }
    
    private void initializeDatabase(
            DriverManagerDataSource dataSource,
            ConfigurationService configuration
    ) throws SQLException {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        boolean fresh = false;
        
        if (configuration.agentRefresh) {
            fresh = true;
        } else {
            List<Map<String, Object>> rows = jdbc.sql("SHOW TABLES").query().listOfRows();
            if (rows.isEmpty()) {
                fresh = true;
            }
        }
        
        if (fresh) {
            Connection conn = dataSource.getConnection();
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/schema.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/data.sql"));
            this.logger.info("Using fresh database state");
        } else {
            this.logger.info("Using previous database state");
        }
    }
}
