package kr.ktb.finn_week6.global.customException;

public class DeletedUserException extends RuntimeException {
    public DeletedUserException(String message) {
        super(message);
    }
}
