package id.my.michaelrk02.smartvote.dao;

import id.my.michaelrk02.smartvote.exceptions.StateInvalidException;
import id.my.michaelrk02.smartvote.model.Ballot;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class BallotDao {
    private JdbcClient jdbc;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }
    
    public Optional<Ballot> findToken(int token) {
        return this.jdbc.sql("SELECT * FROM `ballot` WHERE `token` = ?")
                .param(token)
                .query(Ballot.class)
                .optional();
    }
    
    public Optional<Ballot> findLast() {
        return this.jdbc.sql("SELECT * FROM `ballot` WHERE `id` = (SELECT MAX(`id`) FROM `ballot`)")
                .query(Ballot.class)
                .optional();
    }
    
    public void insert(int token, int candidateId, int agentId, String hash, @Nullable String prevHash) {
        this.jdbc.sql("INSERT INTO `ballot` (`token`, `candidate_id`, `agent_id`, `hash`, `prev_hash`) VALUES (?, ?, ?, ?, ?)")
                .param(token)
                .param(candidateId)
                .param(agentId)
                .param(hash)
                .param(prevHash)
                .update();
    }
    
    public String getState() {
        Optional<Ballot> lastBallot = this.findLast();
        return lastBallot.isPresent() ? lastBallot.get().hash() : "NULL";
    }
    
    public List<Ballot> getData() {
        return this.jdbc.sql("SELECT * FROM `ballot`").query(Ballot.class).list();
    }
    
    public List<Ballot> getData(String lastState) throws StateInvalidException {
        if (lastState.equals("NULL")) {
            return this.getData();
        }
        
        Optional<Integer> lastId = this.jdbc.sql("SELECT `id` FROM `ballot` WHERE `hash` = ?").param(lastState).query(Integer.class).optional();
        if (lastId.isEmpty()) {
            throw new StateInvalidException(lastState);
        }
        
        return this.jdbc.sql("SELECT * FROM `ballot` WHERE `id` > ?")
                .param(lastId.get())
                .query(Ballot.class).list();
    }
    
    public void clear() {
        this.jdbc.sql("DELETE FROM `ballot`").update();
        this.jdbc.sql("ALTER TABLE `ballot` AUTO_INCREMENT = 1").update();
    }
}
