package ru.idemidov.interviewtask;

public class InterviewException extends RuntimeException {
    private String message;

    public String getMessage() {
        return this.message;
    }

    public InterviewException(String message) {
        this.message = message;
    }
}
