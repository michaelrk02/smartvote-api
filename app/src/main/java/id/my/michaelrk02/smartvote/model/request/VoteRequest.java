package id.my.michaelrk02.smartvote.model.request;

public record VoteRequest(
        int token,
        int candidateId
        ) { }
