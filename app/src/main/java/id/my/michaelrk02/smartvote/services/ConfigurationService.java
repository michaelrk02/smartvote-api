package id.my.michaelrk02.smartvote.services;

import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    public final int agentId;
    public final boolean agentRefresh;
    public final int agentFaultyId;
    public final String agentFaultyMode;
    public final boolean agentAutosync;
    public final boolean agentTokenUnchecked;
    
    public ConfigurationService() {
        this.agentId = Integer.parseInt(System.getProperty("agent.id", "0"));
        this.agentRefresh = Boolean.parseBoolean(System.getProperty("agent.refresh", "false"));
        this.agentFaultyId = Integer.parseInt(System.getProperty("agent.faulty.id", "0"));
        this.agentFaultyMode = System.getProperty("agent.faulty.mode", "");
        this.agentAutosync = Boolean.parseBoolean(System.getProperty("agent.autosync", "false"));
        this.agentTokenUnchecked = Boolean.parseBoolean(System.getProperty("agent.token.unchecked", "false"));
    }
    
    public String getFaulty() {
        if (this.agentId == this.agentFaultyId) {
            return this.agentFaultyMode;
        }
        return "";
    }
}
