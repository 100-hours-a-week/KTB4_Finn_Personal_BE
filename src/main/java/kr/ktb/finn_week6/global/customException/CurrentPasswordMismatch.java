package kr.ktb.finn_week6.global.customException;

public class CurrentPasswordMismatch extends RuntimeException {
    public CurrentPasswordMismatch(String message) {
        super(message);
    }
}
