package id.my.michaelrk02.smartvote.dao;

import id.my.michaelrk02.smartvote.model.Ballot;
import id.my.michaelrk02.smartvote.util.Crypto;
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
    
    public void insert(int token, int candidateId, int agentId, @Nullable String prevHash) {
        String hash = Crypto.sha256(String.valueOf(token) + String.valueOf(candidateId) + String.valueOf(agentId) + prevHash);
        this.jdbc.sql("INSERT INTO `ballot` (`token`, `candidate_id`, `agent_id`, `hash`, `prev_hash`) VALUES (?, ?, ?, ?, ?)")
                .param(token)
                .param(candidateId)
                .param(agentId)
                .param(hash)
                .param(prevHash)
                .update();
    }
}
