package id.my.michaelrk02.smartvote;

import id.my.michaelrk02.smartvote.services.ConfigurationService;
import id.my.michaelrk02.smartvote.services.VotingAgentService;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@SpringBootApplication
public class App {
    private Logger logger;
    
    private @Autowired DriverManagerDataSource dataSource;
    private @Autowired ConfigurationService configuration;
    private @Autowired VotingAgentService votingAgent;
    
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            this.logger = LoggerFactory.getLogger(App.class);
            
            this.logger.info("Using database {}", this.dataSource.getUrl());
            
            new Thread(new VotingAgentDispatcher(this.configuration.agentId, this.votingAgent)).start();
        };
    }
    
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        
        SpringApplication.run(App.class, args);
    }
}
