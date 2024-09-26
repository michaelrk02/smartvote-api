package id.my.michaelrk02.smartvote.dao;

import id.my.michaelrk02.smartvote.model.Candidate;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CandidateDao {
    private JdbcClient jdbc;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }
    
    public List<Candidate> getData() {
        return this.jdbc.sql("SELECT * FROM `candidate`").query(Candidate.class).list();
    }
}
