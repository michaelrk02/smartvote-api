package id.my.michaelrk02.smartvote.exceptions;

public class StateUnlockedException extends Exception {
    public StateUnlockedException() {
        super("System state is currently unlocked");
    }
}
