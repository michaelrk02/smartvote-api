package id.my.michaelrk02.smartvote.services;

import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    public final int agentId;
    public final boolean agentInit;
    public final boolean agentFaulty;
    public final boolean agentAutosync;
    
    public ConfigurationService() {
        this.agentId = Integer.parseInt(System.getProperty("agent.id", "0"));
        this.agentInit = Boolean.parseBoolean(System.getProperty("agent.init", "true"));
        this.agentFaulty = Boolean.parseBoolean(System.getProperty("agent.faulty", "false"));
        this.agentAutosync = Boolean.parseBoolean(System.getProperty("agent.autosync", "true"));
    }
}
