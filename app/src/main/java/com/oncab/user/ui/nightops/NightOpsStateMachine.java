package com.oncab.user.ui.nightops;

import androidx.annotation.NonNull;

/**
 * Pure-Java state machine for the Night Ops ride flow. No Android imports so it
 * runs under plain JUnit on the JVM (CI gate: testDebugUnitTest).
 *
 * Phases:  NIGHT_HOME → RIDE_CONFIRMED → DRIVER_ARRIVING → DRIVER_ARRIVED →
 *          IN_TRIP → TRIP_COMPLETE → (rate) → NIGHT_HOME
 * Cancellation is allowed up to and including DRIVER_ARRIVED (the driver is at
 * the curb but boarding has not started); once IN_TRIP the locked fare stands.
 */
public final class NightOpsStateMachine {

    public enum Phase {
        NIGHT_HOME, RIDE_CONFIRMED, DRIVER_ARRIVING, DRIVER_ARRIVED, IN_TRIP,
        TRIP_COMPLETE, CANCELLED
    }

    public enum Event {
        REQUEST_RIDE, DRIVER_ASSIGNED, ETA_ELAPSED, START_TRIP_WITH_OTP,
        ARRIVE_DESTINATION, RATE_SUBMITTED, CANCEL_RIDE
    }

    private Phase phase = Phase.NIGHT_HOME;
    private int failedOtpAttempts = 0;
    private int rating = 0;

    public synchronized Phase phase() {
        return phase;
    }

    /** Attempts left before the OTP pad locks (0 when locked). */
    public synchronized int remainingOtpAttempts() {
        return Math.max(0, NightOpsConfig.MAX_OTP_ATTEMPTS - failedOtpAttempts);
    }

    public synchronized boolean otpLocked() {
        return failedOtpAttempts >= NightOpsConfig.MAX_OTP_ATTEMPTS;
    }

    public synchronized int ratingGiven() {
        return rating;
    }

    /**
     * Fires an event against the current phase.
     *
     * @return true if the transition was accepted
     */
    public synchronized boolean fire(@NonNull Event event) {
        switch (phase) {
            case NIGHT_HOME:
                if (event == Event.REQUEST_RIDE) {
                    phase = Phase.RIDE_CONFIRMED;
                    return true;
                }
                return false;
            case RIDE_CONFIRMED:
                if (event == Event.DRIVER_ASSIGNED) {
                    phase = Phase.DRIVER_ARRIVING;
                    return true;
                }
                if (event == Event.CANCEL_RIDE) {
                    phase = Phase.CANCELLED;
                    return true;
                }
                return false;
            case DRIVER_ARRIVING:
                if (event == Event.ETA_ELAPSED) {
                    phase = Phase.DRIVER_ARRIVED;
                    return true;
                }
                if (event == Event.CANCEL_RIDE) {
                    phase = Phase.CANCELLED;
                    return true;
                }
                return false;
            case DRIVER_ARRIVED:
                if (event == Event.START_TRIP_WITH_OTP) {
                    phase = Phase.IN_TRIP;
                    return true;
                }
                if (event == Event.CANCEL_RIDE) {
                    phase = Phase.CANCELLED;
                    return true;
                }
                return false;
            case IN_TRIP:
                if (event == Event.ARRIVE_DESTINATION) {
                    phase = Phase.TRIP_COMPLETE;
                    return true;
                }
                return false;
            case TRIP_COMPLETE:
                if (event == Event.RATE_SUBMITTED) {
                    phase = Phase.NIGHT_HOME;
                    return true;
                }
                return false;
            case CANCELLED:
            default:
                return false;
        }
    }

    /**
     * Verifies the driver's OTP. Only meaningful in DRIVER_ARRIVED; a correct
     * code moves the machine to IN_TRIP (callers must then fire
     * START_TRIP_WITH_OTP for bookkeeping), a wrong one consumes an attempt.
     *
     * @return true when the code matches
     */
    public synchronized boolean verifyOtp(@NonNull String input) {
        if (phase != Phase.DRIVER_ARRIVED || otpLocked() || input == null) {
            return false;
        }
        if (NightOpsConfig.TRIP_OTP.equals(input.trim())) {
            return true;
        }
        failedOtpAttempts++;
        return false;
    }

    /** Records the post-trip driver rating (1..5). */
    public synchronized boolean submitRating(int stars) {
        if (phase != Phase.TRIP_COMPLETE || stars < 1 || stars > 5) {
            return false;
        }
        rating = stars;
        return fire(Event.RATE_SUBMITTED);
    }

    /** The locked fare invariant: identical in every phase. */
    public synchronized int lockedFare() {
        return NightOpsConfig.LOCKED_FARE;
    }

    /** Resets to a fresh night (used by tests and after rating loop). */
    public synchronized void reset() {
        phase = Phase.NIGHT_HOME;
        failedOtpAttempts = 0;
        rating = 0;
    }
}
