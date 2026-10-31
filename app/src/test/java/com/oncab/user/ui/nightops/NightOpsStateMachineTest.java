package com.oncab.user.ui.nightops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/** Gauntlet gate: every transition of the Night Ops ride flow. */
public class NightOpsStateMachineTest {

    private NightOpsStateMachine sm;

    @Before
    public void setUp() {
        sm = new NightOpsStateMachine();
    }

    @Test
    public void startsAtNightHome() {
        assertEquals(NightOpsStateMachine.Phase.NIGHT_HOME, sm.phase());
    }

    @Test
    public void happyPathWalksEveryPhase() {
        assertTrue(sm.fire(NightOpsStateMachine.Event.REQUEST_RIDE));
        assertEquals(NightOpsStateMachine.Phase.RIDE_CONFIRMED, sm.phase());

        assertTrue(sm.fire(NightOpsStateMachine.Event.DRIVER_ASSIGNED));
        assertEquals(NightOpsStateMachine.Phase.DRIVER_ARRIVING, sm.phase());

        assertTrue(sm.fire(NightOpsStateMachine.Event.ETA_ELAPSED));
        assertEquals(NightOpsStateMachine.Phase.DRIVER_ARRIVED, sm.phase());

        assertTrue(sm.verifyOtp(NightOpsConfig.TRIP_OTP));
        assertTrue(sm.fire(NightOpsStateMachine.Event.START_TRIP_WITH_OTP));
        assertEquals(NightOpsStateMachine.Phase.IN_TRIP, sm.phase());

        assertTrue(sm.fire(NightOpsStateMachine.Event.ARRIVE_DESTINATION));
        assertEquals(NightOpsStateMachine.Phase.TRIP_COMPLETE, sm.phase());

        assertTrue(sm.submitRating(5));
        assertEquals(NightOpsStateMachine.Phase.NIGHT_HOME, sm.phase());
        assertEquals(5, sm.ratingGiven());
    }

    @Test
    public void invalidTransitionsAreRejected() {
        assertFalse(sm.fire(NightOpsStateMachine.Event.START_TRIP_WITH_OTP));
        assertFalse(sm.fire(NightOpsStateMachine.Event.ARRIVE_DESTINATION));
        assertFalse(sm.fire(NightOpsStateMachine.Event.RATE_SUBMITTED));
        assertEquals(NightOpsStateMachine.Phase.NIGHT_HOME, sm.phase());
    }

    @Test
    public void otpOnlyVerifiedOnceDriverArrived() {
        assertTrue(sm.fire(NightOpsStateMachine.Event.REQUEST_RIDE));
        assertFalse("OTP must not verify before arrival", sm.verifyOtp(NightOpsConfig.TRIP_OTP));
        sm.fire(NightOpsStateMachine.Event.DRIVER_ASSIGNED);
        assertFalse(sm.verifyOtp(NightOpsConfig.TRIP_OTP));
        sm.fire(NightOpsStateMachine.Event.ETA_ELAPSED);
        assertTrue(sm.verifyOtp(NightOpsConfig.TRIP_OTP));
    }

    @Test
    public void wrongOtpConsumesAttemptsThenLocks() {
        walkTo(NightOpsStateMachine.Phase.DRIVER_ARRIVED);
        assertFalse(sm.verifyOtp("0000"));
        assertEquals(2, sm.remainingOtpAttempts());
        assertFalse(sm.verifyOtp("1111"));
        assertEquals(1, sm.remainingOtpAttempts());
        assertFalse(sm.verifyOtp("2222"));
        assertEquals(0, sm.remainingOtpAttempts());
        assertTrue(sm.otpLocked());
        assertFalse("locked pad must reject even the right code",
                sm.verifyOtp(NightOpsConfig.TRIP_OTP));
    }

    @Test
    public void cancelAllowedUntilBoarding() {
        assertTrue(sm.fire(NightOpsStateMachine.Event.REQUEST_RIDE));
        assertTrue(sm.fire(NightOpsStateMachine.Event.CANCEL_RIDE));
        assertEquals(NightOpsStateMachine.Phase.CANCELLED, sm.phase());
        assertFalse("cancelled ride is terminal",
                sm.fire(NightOpsStateMachine.Event.REQUEST_RIDE));
    }

    @Test
    public void cancelStillAllowedAfterDriverArrived() {
        // Driver at the curb but boarding has not started: cancel stands.
        walkTo(NightOpsStateMachine.Phase.DRIVER_ARRIVED);
        assertTrue(sm.fire(NightOpsStateMachine.Event.CANCEL_RIDE));
        assertEquals(NightOpsStateMachine.Phase.CANCELLED, sm.phase());
    }

    @Test
    public void cancelBlockedOnceInTrip() {
        walkTo(NightOpsStateMachine.Phase.IN_TRIP);
        assertFalse(sm.fire(NightOpsStateMachine.Event.CANCEL_RIDE));
        assertEquals(NightOpsStateMachine.Phase.IN_TRIP, sm.phase());
    }

    @Test
    public void lockedFareInvariantAcrossPhases() {
        assertEquals(NightOpsConfig.LOCKED_FARE, sm.lockedFare());
        walkTo(NightOpsStateMachine.Phase.IN_TRIP);
        assertEquals("fare must never surge mid-ride",
                NightOpsConfig.LOCKED_FARE, sm.lockedFare());
    }

    @Test
    public void ratingOnlyAcceptedOnCompleteAndInRange() {
        assertFalse(sm.submitRating(5));
        walkTo(NightOpsStateMachine.Phase.TRIP_COMPLETE);
        assertFalse(sm.submitRating(0));
        assertFalse(sm.submitRating(6));
        assertTrue(sm.submitRating(4));
        assertEquals(NightOpsStateMachine.Phase.NIGHT_HOME, sm.phase());
    }

    @Test
    public void resetReturnsToFreshNight() {
        walkTo(NightOpsStateMachine.Phase.IN_TRIP);
        sm.reset();
        assertEquals(NightOpsStateMachine.Phase.NIGHT_HOME, sm.phase());
        assertEquals(3, sm.remainingOtpAttempts());
        assertFalse(sm.otpLocked());
    }

    private void walkTo(NightOpsStateMachine.Phase target) {
        while (sm.phase() != target) {
            switch (sm.phase()) {
                case NIGHT_HOME:
                    assertTrue(sm.fire(NightOpsStateMachine.Event.REQUEST_RIDE));
                    break;
                case RIDE_CONFIRMED:
                    assertTrue(sm.fire(NightOpsStateMachine.Event.DRIVER_ASSIGNED));
                    break;
                case DRIVER_ARRIVING:
                    assertTrue(sm.fire(NightOpsStateMachine.Event.ETA_ELAPSED));
                    break;
                case DRIVER_ARRIVED:
                    assertTrue(sm.verifyOtp(NightOpsConfig.TRIP_OTP));
                    assertTrue(sm.fire(NightOpsStateMachine.Event.START_TRIP_WITH_OTP));
                    break;
                case IN_TRIP:
                    assertTrue(sm.fire(NightOpsStateMachine.Event.ARRIVE_DESTINATION));
                    break;
                default:
                    throw new IllegalStateException("cannot walk to " + target);
            }
        }
        assertEquals(target, sm.phase());
    }
}
