package com.oncab.user.ui.nightops;

/** Immutable constants for the Night Ops flow (design contract of proto3). */
public final class NightOpsConfig {
    /** 4-digit start-trip OTP shown to the rider and required from the driver. */
    public static final String TRIP_OTP = "5731";
    public static final String DRIVER_NAME = "Sanjib Das";
    public static final double DRIVER_RATING = 4.9;
    public static final String DRIVER_VEHICLE = "White Hyundai Aura";
    public static final String DRIVER_PLATE = "WB 02 AB 4412";
    public static final String DRIVER_PHONE = "+919000000000"; // ACTION_DIAL only, never auto-called
    public static final int LOCKED_FARE = 160;
    public static final int WALLET_BALANCE = 240;
    public static final int ETA_MINUTES = 4;
    public static final int CAB_COUNT_NEARBY = 12;
    public static final float TRIP_KM = 2.8f;
    public static final int SOS_COUNTDOWN_SECONDS = 3;
    public static final String POLICE_NUMBER = "100";
    /** Pickup: Park Street, Kolkata. Drop: Esplanade. */
    public static final String PICKUP_LOCATION = "22.5535,88.3520";
    public static final int MAX_OTP_ATTEMPTS = 3;

    /** Wallet balance after the locked fare is charged. */
    public static int walletAfterRide() {
        return walletAfterRide(LOCKED_FARE, WALLET_BALANCE);
    }

    /** Package-private seam so tests can probe the guard without reflection. */
    static int walletAfterRide(int lockedFare, int walletBalance) {
        if (lockedFare > walletBalance) {
            throw new IllegalStateException("Locked fare cannot exceed wallet balance");
        }
        return walletBalance - lockedFare;
    }

    private NightOpsConfig() { }
}
