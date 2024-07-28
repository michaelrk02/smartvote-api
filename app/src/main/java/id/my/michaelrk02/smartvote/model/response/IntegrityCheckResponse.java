package id.my.michaelrk02.smartvote.model.response;

public record IntegrityCheckResponse(
        boolean valid,
        String message
        ) { }
