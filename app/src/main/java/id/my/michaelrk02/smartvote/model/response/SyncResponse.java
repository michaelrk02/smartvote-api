package id.my.michaelrk02.smartvote.model.response;

public record SyncResponse(
        String localState,
        String globalState,
        boolean synced
        ) { }
