package id.my.michaelrk02.smartvote;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class AppConfig {
    @Bean
    public DriverManagerDataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(System.getProperty("db.url"));
        ds.setUsername(System.getProperty("db.username"));
        ds.setPassword(System.getProperty("db.password"));
        return ds;
    }
}
