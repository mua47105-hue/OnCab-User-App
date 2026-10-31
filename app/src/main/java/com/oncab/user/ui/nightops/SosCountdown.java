package com.oncab.user.ui.nightops;

import androidx.annotation.NonNull;

/**
 * Pure-Java 3-second SOS countdown. The UI drives {@link #tick()} from a timer;
 * this class owns the semantics (expiry fires exactly once, cancel is final
 * until restart). Unit-tested on the JVM.
 */
public final class SosCountdown {

    /** Receives countdown progress. */
    public interface Sink {
        /** Called on every tick with the seconds left (counting includes start). */
        void onTick(int secondsLeft);

        /** Called exactly once when the countdown reaches zero. */
        void onExpired();
    }

    private final int totalSeconds;
    private int secondsLeft;
    private boolean running;
    private boolean expired;

    public SosCountdown(int totalSeconds) {
        if (totalSeconds <= 0) {
            throw new IllegalArgumentException("totalSeconds must be > 0");
        }
        this.totalSeconds = totalSeconds;
        this.secondsLeft = totalSeconds;
    }

    public int totalSeconds() {
        return totalSeconds;
    }

    public int secondsLeft() {
        return secondsLeft;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isExpired() {
        return expired;
    }

    /** Starts (or restarts) the countdown. */
    public void start(@NonNull Sink sink) {
        secondsLeft = totalSeconds;
        expired = false;
        running = true;
        sink.onTick(secondsLeft);
    }

    /**
     * Advances one second.
     *
     * @return seconds left after the tick; 0 on and after expiry
     */
    public int tick(@NonNull Sink sink) {
        if (!running || expired) {
            return secondsLeft;
        }
        secondsLeft--;
        if (secondsLeft <= 0) {
            secondsLeft = 0;
            running = false;
            expired = true;
            sink.onTick(0);
            sink.onExpired();
        } else {
            sink.onTick(secondsLeft);
        }
        return secondsLeft;
    }

    /** Cancels the countdown; a cancelled countdown cannot expire. */
    public void cancel() {
        running = false;
        secondsLeft = totalSeconds;
    }
}
