package com.oncab.user.ui.nightops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Gauntlet gate: design-contract constants and wallet arithmetic. */
public class NightOpsConfigTest {

    @Test
    public void otpIsFourDigits() {
        assertEquals(4, NightOpsConfig.TRIP_OTP.length());
        assertTrue(NightOpsConfig.TRIP_OTP.matches("\\d{4}"));
        assertEquals("5731", NightOpsConfig.TRIP_OTP);
    }

    @Test
    public void designContractValues() {
        assertEquals("Sanjib Das", NightOpsConfig.DRIVER_NAME);
        assertEquals(4.9, NightOpsConfig.DRIVER_RATING, 0.0001);
        assertEquals("White Hyundai Aura", NightOpsConfig.DRIVER_VEHICLE);
        assertEquals("WB 02 AB 4412", NightOpsConfig.DRIVER_PLATE);
        assertEquals(160, NightOpsConfig.LOCKED_FARE);
        assertEquals(240, NightOpsConfig.WALLET_BALANCE);
        assertEquals(4, NightOpsConfig.ETA_MINUTES);
        assertEquals(12, NightOpsConfig.CAB_COUNT_NEARBY);
        assertEquals(2.8f, NightOpsConfig.TRIP_KM, 0.0001);
        assertEquals(3, NightOpsConfig.SOS_COUNTDOWN_SECONDS);
        assertEquals("100", NightOpsConfig.POLICE_NUMBER);
    }

    @Test
    public void walletMathIsExact() {
        assertEquals(80, NightOpsConfig.walletAfterRide());
    }

    @Test(expected = IllegalStateException.class)
    public void fareAboveWalletIsRejected() {
        // Direct probe of the guard through the package-private seam.
        NightOpsConfig.walletAfterRide(999999, NightOpsConfig.WALLET_BALANCE);
    }

    @Test
    public void fareEqualWalletIsAllowed() {
        assertEquals(0, NightOpsConfig.walletAfterRide(
                NightOpsConfig.LOCKED_FARE, NightOpsConfig.LOCKED_FARE));
    }
}
