package id.my.michaelrk02.smartvote;

import bftsmart.tom.ServiceReplica;
import id.my.michaelrk02.smartvote.services.VotingAgentService;

public class VotingAgentDispatcher implements Runnable {
    private final int agentId;
    private final VotingAgentService service;
    
    public VotingAgentDispatcher(int agentId, VotingAgentService service) {
        this.agentId = agentId;
        this.service = service;
    }
    
    @Override
    public void run() {
        new ServiceReplica(this.agentId, this.service, this.service);
    }
}
