package id.my.michaelrk02.smartvote.exceptions;

public class TokenInvalidException extends Exception {
    public TokenInvalidException(int id) {
        super("Invalid token: " + id);
    }
}
