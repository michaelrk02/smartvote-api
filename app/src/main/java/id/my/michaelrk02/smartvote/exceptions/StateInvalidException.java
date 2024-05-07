package id.my.michaelrk02.smartvote.exceptions;

public class StateInvalidException extends Exception {
    public StateInvalidException(String state) {
        super("Invalid state: " + state);
    }
}
