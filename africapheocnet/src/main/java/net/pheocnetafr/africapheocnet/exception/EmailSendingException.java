package net.pheocnetafr.africapheocnet.exception;

public class EmailSendingException extends RuntimeException {

    private final int errorCode;

    public EmailSendingException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
