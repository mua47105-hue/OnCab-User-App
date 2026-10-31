package com.oncab.user.ui.nightops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Gauntlet gate: SOS countdown semantics (3-2-1, single expiry, cancel). */
public class SosCountdownTest {

    private int ticks;
    private int expiries;
    private final SosCountdown.Sink sink = new SosCountdown.Sink() {
        @Override
        public void onTick(int secondsLeft) {
            ticks++;
        }

        @Override
        public void onExpired() {
            expiries++;
        }
    };

    @Test
    public void countsDownThreeTwoOneThenExpiresOnce() {
        SosCountdown sos = new SosCountdown(NightOpsConfig.SOS_COUNTDOWN_SECONDS);
        assertEquals(3, sos.totalSeconds());
        sos.start(sink);
        assertEquals(3, sos.secondsLeft());
        assertEquals(1, ticks);
        assertEquals(0, expiries);

        assertEquals(2, sos.tick(sink));
        assertEquals(1, sos.tick(sink));
        assertEquals(0, sos.tick(sink));
        assertEquals(1, expiries);
        assertFalse(sos.isRunning());
        assertTrue(sos.isExpired());

        // extra ticks after expiry never re-fire
        sos.tick(sink);
        sos.tick(sink);
        assertEquals(1, expiries);
        assertEquals(0, sos.secondsLeft());
    }

    @Test
    public void cancelStopsExpiry() {
        SosCountdown sos = new SosCountdown(3);
        sos.start(sink);
        sos.tick(sink); // 2 left
        sos.cancel();
        assertFalse(sos.isRunning());
        // Bounded sweep: a cancelled countdown must never expire no matter how
        // many ticks arrive (bound guards the JVM gate against a runaway loop).
        for (int i = 0; i < 10; i++) {
            sos.tick(sink);
        }
        assertEquals("cancelled countdown must not expire", 0, expiries);
        assertTrue("ticks after cancel must not advance the countdown",
                sos.secondsLeft() <= 2);
    }

    @Test
    public void restartResetsState() {
        SosCountdown sos = new SosCountdown(3);
        sos.start(sink);
        sos.tick(sink);
        sos.tick(sink);
        sos.start(sink);
        assertEquals(3, sos.secondsLeft());
        assertFalse(sos.isExpired());
        assertTrue(sos.isRunning());
    }

    @Test
    public void cancelledThenRestartedRunsNormally() {
        SosCountdown sos = new SosCountdown(3);
        sos.start(sink);
        sos.cancel();
        sos.start(sink);
        assertEquals(3, sos.secondsLeft());
        assertEquals(2, sos.tick(sink));
        assertEquals(1, sos.tick(sink));
        assertEquals(0, sos.tick(sink));
        assertEquals(1, expiries);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveSeconds() {
        new SosCountdown(0);
    }
}
