package com.app.gym.otp;

public class GymOtpFailure extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final String nextAction;

    public GymOtpFailure(
            int status,
            String message,
            String nextAction) {

        super(message);

        this.status = status;
        this.nextAction = nextAction;
    }

    public int getStatus() {
        return status;
    }

    public String getNextAction() {
        return nextAction;
    }
}