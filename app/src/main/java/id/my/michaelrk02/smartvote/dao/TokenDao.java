package id.my.michaelrk02.smartvote.dao;

import id.my.michaelrk02.smartvote.util.Crypto;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class TokenDao {
    private JdbcClient jdbc;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }
    
    public boolean exists(int id) {
        String idHash = Crypto.sha256(String.valueOf(id));
        return this.jdbc.sql("SELECT EXISTS(SELECT * FROM `token` WHERE `id_hash` = ?)")
                .param(idHash)
                .query(Boolean.class)
                .single();
    }
    
    public void insert(int id) {
        String idHash = Crypto.sha256(String.valueOf(id));
        this.jdbc.sql("INSERT INTO `token` (`id_hash`) VALUES (?)")
                .param(idHash)
                .update();
    }
}
