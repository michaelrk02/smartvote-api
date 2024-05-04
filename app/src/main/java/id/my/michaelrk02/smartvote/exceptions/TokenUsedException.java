package id.my.michaelrk02.smartvote.exceptions;

public class TokenUsedException extends Exception {
    public TokenUsedException(int token) {
        super("Token " + token + " is already used");
    }
}
