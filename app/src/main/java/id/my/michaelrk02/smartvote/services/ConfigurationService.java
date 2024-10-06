package id.my.michaelrk02.smartvote.services;

import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    public final int agentId;
    public final boolean agentRefresh;
    public final String agentFaulty;
    public final boolean agentAutosync;
    public final boolean agentTokenUnchecked;
    
    public ConfigurationService() {
        this.agentId = Integer.parseInt(System.getProperty("agent.id", "0"));
        this.agentRefresh = Boolean.parseBoolean(System.getProperty("agent.refresh", "false"));
        this.agentFaulty = System.getProperty("agent.faulty", "");
        this.agentAutosync = Boolean.parseBoolean(System.getProperty("agent.autosync", "true"));
        this.agentTokenUnchecked = Boolean.parseBoolean(System.getProperty("agent.token.unchecked", "false"));
    }
}
