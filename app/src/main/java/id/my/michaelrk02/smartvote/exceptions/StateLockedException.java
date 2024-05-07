package id.my.michaelrk02.smartvote.exceptions;

public class StateLockedException extends Exception {
    public StateLockedException() {
        super("System state is currently locked");
    }
}
