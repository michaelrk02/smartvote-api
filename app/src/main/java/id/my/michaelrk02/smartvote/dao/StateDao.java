package id.my.michaelrk02.smartvote.dao;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class StateDao {
    private JdbcClient jdbc;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }
    
    public boolean isLocked() {
        return this.selectFor("locked").query(Boolean.class).single();
    }
    
    public void setLocked(boolean locked) {
        this.updateFor("locked").param(locked).update();
    }
    
    private JdbcClient.StatementSpec selectFor(String key) {
        return this.jdbc.sql("SELECT `value` FROM `state` WHERE `key` = '" + key + "'");
    }
    
    private JdbcClient.StatementSpec updateFor(String key) {
        return this.jdbc.sql("UPDATE `state` SET `value` = ? WHERE `key` = '" + key + "'");
    }
}
