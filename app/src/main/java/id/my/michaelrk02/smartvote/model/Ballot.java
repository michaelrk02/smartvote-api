package id.my.michaelrk02.smartvote.model;

import javax.annotation.Nullable;

public record Ballot(
        int id,
        int token,
        int candidateId,
        int agentId,
        String hash,
        @Nullable String prevHash
        ) { }
