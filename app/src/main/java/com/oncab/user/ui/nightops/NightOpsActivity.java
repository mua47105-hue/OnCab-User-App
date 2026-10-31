package com.oncab.user.ui.nightops;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.oncab.user.MainActivity;
import com.oncab.user.R;
import com.oncab.user.databinding.ActivityNightOpsBinding;
import com.oncab.user.databinding.DialogNightOtpBinding;

/**
 * Night Ops — full night-mode ride flow, implemented from the approved
 * proto3 design: request → confirmed (OTP 5731, driver Sanjib Das, locked
 * fare ₹160) → arriving → OTP boarding → on-trip → completion + rating,
 * with a live dark map, SOS overlay and real Android intents (dial, share,
 * SMS). Ride progression is driven by {@link NightOpsStateMachine}.
 */
public class NightOpsActivity extends AppCompatActivity {

    public static final String EXTRA_NAV_DESTINATION = "night_nav_destination";
    private static final long ETA_TOTAL_SECONDS =
            NightOpsConfig.ETA_MINUTES * 60L;
    private static final long ON_TRIP_SETTLE_MS = 5000L;

    private ActivityNightOpsBinding binding;
    private final NightOpsStateMachine machine = new NightOpsStateMachine();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SosCountdown sosCountdown;
    private SosCountdown.Sink sosSink;
    private CountDownTimer etaTimer;
    private AlertDialog otpDialog;
    private int selectedStars = 0;
    private boolean sosActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNightOpsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        renderStaticContract();
        wireHeaderAndMap();
        wireHomePanel();
        wireConfirmedPanel();
        wireCompletePanel();
        wireBottomNav();
        wireSosOverlay();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NightOpsActivity.this.handleBack();
            }
        });

        // Launch directly in the approved design state: ride confirmed with the
        // driver already assigned (the bar screen), ETA running.
        machine.fire(NightOpsStateMachine.Event.REQUEST_RIDE);
        machine.fire(NightOpsStateMachine.Event.DRIVER_ASSIGNED);
        showPanel(binding.panelConfirmed, binding.panelHome);
        startEtaCountdown();
    }

    // ------------------------------------------------------------------ setup

    private void renderStaticContract() {
        // OTP tiles: 5 · 7 · 3 · 1 (design contract)
        String otp = NightOpsConfig.TRIP_OTP;
        binding.otpTile1.setText(String.valueOf(otp.charAt(0)));
        binding.otpTile2.setText(String.valueOf(otp.charAt(1)));
        binding.otpTile3.setText(String.valueOf(otp.charAt(2)));
        binding.otpTile4.setText(String.valueOf(otp.charAt(3)));
        binding.otpCard.setContentDescription(getString(R.string.night_ops_otp_cd, otp));

        binding.tvNightBanner.setText(getString(
                R.string.night_ops_banner_night_mode, NightOpsConfig.CAB_COUNT_NEARBY));
        binding.tvFarePreview.setText(getString(
                R.string.night_ops_fare_preview, NightOpsConfig.LOCKED_FARE));
        binding.tvFareAmount.setText(getString(
                R.string.night_ops_fare_amount, NightOpsConfig.LOCKED_FARE));
        binding.tvWallet.setText(getString(
                R.string.night_ops_wallet, NightOpsConfig.WALLET_BALANCE));
        binding.tvEtaValue.setText(getString(
                R.string.night_ops_eta_minutes, NightOpsConfig.ETA_MINUTES));
        binding.tvFareCharged.setText(getString(
                R.string.night_ops_fare_charged,
                NightOpsConfig.LOCKED_FARE, NightOpsConfig.walletAfterRide()));
        binding.tvTripKm.setText(getString(
                R.string.night_ops_trip_km, String.valueOf(NightOpsConfig.TRIP_KM)));
    }

    private void wireHeaderAndMap() {
        // Pulsing "safe" dots — GPS pill and night banner (design: animate-ping)
        loopPulse(binding.gpsDot, 0.35f, 1f, 900L);
        loopPulse(binding.bannerDotPing, 0.3f, 0.85f, 1400L);

        binding.nightMap.start();
        binding.btnRecenter.setOnClickListener(v -> binding.nightMap.recenter());
        binding.btnSecurity.setOnClickListener(v -> showSafeguardInfo());
    }

    private void wireHomePanel() {
        binding.btnRequest.setOnClickListener(v -> requestRide());
    }

    private void wireConfirmedPanel() {
        binding.btnShareTrip.setOnClickListener(v -> shareTrip());
        binding.btnSos.setOnClickListener(v -> openSos());
        binding.btnCallDriver.setOnClickListener(v -> callDriver());
        binding.btnChatDriver.setOnClickListener(v -> chatDriver());
        binding.btnStartTrip.setOnClickListener(v -> showOtpDialog());
    }

    private void wireCompletePanel() {
        View[] stars = {binding.star1, binding.star2, binding.star3, binding.star4, binding.star5};
        for (int i = 0; i < stars.length; i++) {
            final int value = i + 1;
            stars[i].setOnClickListener(v -> {
                selectedStars = value;
                renderStars();
            });
        }
        binding.btnRateSubmit.setOnClickListener(v -> {
            if (machine.submitRating(selectedStars)) {
                backToNightHome();
            } else {
                Toast.makeText(this,
                        getString(R.string.night_ops_rate_heading), Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnRateDone.setOnClickListener(v -> backToNightHome());
    }

    private void wireBottomNav() {
        binding.navHome.setOnClickListener(v -> finishWithSlide());
        binding.navRides.setOnClickListener(v -> navigateToMain(R.id.navigation_booking_history));
        binding.navBus.setOnClickListener(v -> navigateToMain(R.id.navigation_shuttle_bookings));
        binding.navProfile.setOnClickListener(v -> navigateToMain(R.id.navigation_account));
        binding.navConnect.setOnClickListener(v -> navigateToMain(R.id.navigation_inbox));
    }

    private void wireSosOverlay() {
        binding.btnCancelSos.setOnClickListener(v -> cancelSos());
        binding.btnCallPolice.setOnClickListener(v -> dial(NightOpsConfig.POLICE_NUMBER));
        binding.btnShareSms.setOnClickListener(v -> shareSosSms());
        loopPulse(binding.sosRing, 0.9f, 1.12f, 650L);
    }

    // ------------------------------------------------------------- ride flow

    private void requestRide() {
        if (!machine.fire(NightOpsStateMachine.Event.REQUEST_RIDE)) {
            return;
        }
        showPanel(binding.panelConfirmed, binding.panelHome);
        binding.panelConfirmed.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.night_rise_in));

        // Driver assigned right after confirmation (animated deck is already up)
        handler.postDelayed(() -> {
            if (isFinishing() || machine.phase() != NightOpsStateMachine.Phase.RIDE_CONFIRMED) {
                return;
            }
            machine.fire(NightOpsStateMachine.Event.DRIVER_ASSIGNED);
            binding.tvConfirmedSub.setText(getString(R.string.night_ops_confirmed_sub));
            startEtaCountdown();
        }, 900L);
    }

    private void startEtaCountdown() {
        stopEtaTimer();
        binding.tvEtaLabel.setText(getString(R.string.night_ops_arriving_label));
        binding.tvEtaValue.setText(getString(R.string.night_ops_eta_minutes, NightOpsConfig.ETA_MINUTES));
        etaTimer = new CountDownTimer(ETA_TOTAL_SECONDS * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (machine.phase() != NightOpsStateMachine.Phase.DRIVER_ARRIVING) {
                    return;
                }
                int secs = (int) (millisUntilFinished / 1000L);
                if (secs >= 60) {
                    binding.tvEtaValue.setText(getString(R.string.night_ops_eta_timer,
                            secs / 60, secs % 60));
                } else {
                    binding.tvEtaValue.setText(getString(R.string.night_ops_eta_minutes,
                            Math.max(1, secs / 60)));
                }
            }

            @Override
            public void onFinish() {
                if (machine.phase() == NightOpsStateMachine.Phase.DRIVER_ARRIVING
                        && machine.fire(NightOpsStateMachine.Event.ETA_ELAPSED)) {
                    onDriverArrived();
                }
            }
        }.start();
    }

    private void onDriverArrived() {
        stopEtaTimer();
        binding.tvEtaLabel.setText(getString(R.string.night_ops_driver_arrived));
        binding.tvEtaValue.setText(getString(R.string.night_ops_arrived_now));
        binding.btnStartTrip.setVisibility(View.VISIBLE);
        binding.btnStartTrip.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.night_rise_in));
        loopPulse(binding.otpCard, 0.985f, 1.01f, 1100L);
    }

    private void showOtpDialog() {
        if (machine.phase() != NightOpsStateMachine.Phase.DRIVER_ARRIVED) {
            return;
        }
        if (machine.otpLocked()) {
            Toast.makeText(this, getString(R.string.night_ops_otp_locked),
                    Toast.LENGTH_LONG).show();
            return;
        }
        DialogNightOtpBinding dialog = DialogNightOtpBinding.inflate(getLayoutInflater());
        otpDialog = new AlertDialog.Builder(this)
                .setView(dialog.getRoot())
                .create();
        if (otpDialog.getWindow() != null) {
            otpDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.btnOtpCancel.setOnClickListener(v -> {
            if (otpDialog != null) {
                otpDialog.dismiss();
            }
        });
        dialog.btnOtpVerify.setOnClickListener(v -> {
            String code = dialog.pinView.getText() == null
                    ? "" : dialog.pinView.getText().toString();
            if (machine.verifyOtp(code)) {
                otpDialog.dismiss();
                machine.fire(NightOpsStateMachine.Event.START_TRIP_WITH_OTP);
                onTripStarted();
            } else if (machine.otpLocked()) {
                dialog.tvOtpDialogError.setVisibility(View.VISIBLE);
                dialog.tvOtpDialogError.setText(getString(R.string.night_ops_otp_locked));
            } else {
                dialog.tvOtpDialogError.setVisibility(View.VISIBLE);
                dialog.tvOtpDialogError.setText(getString(
                        R.string.night_ops_otp_wrong, machine.remainingOtpAttempts()));
                dialog.pinView.setText("");
            }
        });
        otpDialog.show();
    }

    private void onTripStarted() {
        binding.btnStartTrip.setVisibility(View.GONE);
        binding.tvEtaLabel.setText(getString(R.string.night_ops_on_trip));
        binding.tvEtaValue.setText(getString(R.string.night_ops_trip_km,
                String.valueOf(NightOpsConfig.TRIP_KM)));
        handler.postDelayed(() -> {
            if (isFinishing() || machine.phase() != NightOpsStateMachine.Phase.IN_TRIP) {
                return;
            }
            if (machine.fire(NightOpsStateMachine.Event.ARRIVE_DESTINATION)) {
                onTripComplete();
            }
        }, ON_TRIP_SETTLE_MS);
    }

    private void onTripComplete() {
        showPanel(binding.panelComplete, binding.panelConfirmed);
        binding.panelComplete.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.night_rise_in));
        selectedStars = 0;
        renderStars();
    }

    private void backToNightHome() {
        machine.reset();
        stopEtaTimer();
        showPanel(binding.panelHome, binding.panelComplete);
        binding.panelHome.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.night_rise_in));
    }

    // ---------------------------------------------------------------- actions

    private void shareTrip() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.night_ops_share_subject));
        send.putExtra(Intent.EXTRA_TEXT, getString(R.string.night_ops_share_body));
        startActivity(Intent.createChooser(send, getString(R.string.night_ops_share_via)));
    }

    private void callDriver() {
        dial(NightOpsConfig.DRIVER_PHONE);
    }

    private void chatDriver() {
        String[] quick = {
                getString(R.string.night_ops_chat_q1),
                getString(R.string.night_ops_chat_q2),
                getString(R.string.night_ops_chat_q3)
        };
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.night_ops_chat_title))
                .setItems(quick, (d, which) -> Toast.makeText(this,
                        getString(R.string.night_ops_chat_sent), Toast.LENGTH_SHORT).show())
                .show();
    }

    private void showSafeguardInfo() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.night_ops_safeguard))
                .setMessage(getString(R.string.night_ops_safeguard_cd))
                .setPositiveButton(getString(R.string.night_ops_sos_cancel), null)
                .show();
    }

    private void dial(String number) {
        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        startActivity(dial);
    }

    private void shareSosSms() {
        String body = getString(R.string.night_ops_sos_body, NightOpsConfig.PICKUP_LOCATION);
        Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
        sms.putExtra("sms_body", body);
        startActivity(sms);
    }

    // -------------------------------------------------------------------- SOS

    private void openSos() {
        sosActive = true;
        sosCountdown = new SosCountdown(NightOpsConfig.SOS_COUNTDOWN_SECONDS);
        binding.sosOverlay.setVisibility(View.VISIBLE);
        binding.sosOverlay.setAlpha(0f);
        binding.sosOverlay.animate().alpha(1f).setDuration(220L)
                .setInterpolator(new DecelerateInterpolator()).start();
        binding.tvSosExpired.setVisibility(View.GONE);
        binding.tvSosCountdown.setVisibility(View.VISIBLE);
        binding.btnCancelSos.setVisibility(View.VISIBLE);
        sosCountdown.start(sosSink());
        tickSos();
    }

    /** One sink drives both the initial tick and every timer tick. */
    private SosCountdown.Sink sosSink() {
        if (sosSink == null) {
            sosSink = new SosCountdown.Sink() {
                @Override
                public void onTick(int secondsLeft) {
                    if (sosActive) {
                        binding.tvSosCountdown.setText(getString(
                                R.string.night_ops_sos_countdown, Math.max(secondsLeft, 1)));
                    }
                }

                @Override
                public void onExpired() {
                    if (!sosActive) {
                        return;
                    }
                    binding.tvSosCountdown.setVisibility(View.GONE);
                    binding.btnCancelSos.setVisibility(View.GONE);
                    binding.tvSosExpired.setVisibility(View.VISIBLE);
                    binding.tvSosExpired.startAnimation(AnimationUtils.loadAnimation(
                            NightOpsActivity.this, R.anim.night_fade_in));
                    vibrate();
                }
            };
        }
        return sosSink;
    }

    private void tickSos() {
        if (!sosActive) {
            return;
        }
        handler.postDelayed(() -> {
            if (!sosActive || sosCountdown == null) {
                return;
            }
            sosCountdown.tick(sosSink());
            if (sosCountdown.isRunning()) {
                tickSos();
            }
        }, 1000L);
    }

    private void cancelSos() {
        sosActive = false;
        if (sosCountdown != null) {
            sosCountdown.cancel();
        }
        binding.sosOverlay.animate().alpha(0f).setDuration(200L).withEndAction(() ->
                binding.sosOverlay.setVisibility(View.GONE)).start();
        Toast.makeText(this, getString(R.string.night_ops_sos_cancelled),
                Toast.LENGTH_SHORT).show();
    }

    private void vibrate() {
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vib == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(350L,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vib.vibrate(350L);
        }
    }

    // ---------------------------------------------------------------- helpers

    private void showPanel(View show, View hide) {
        if (hide != null) {
            hide.setVisibility(View.GONE);
        }
        show.setVisibility(View.VISIBLE);
    }

    private void renderStars() {
        View[] stars = {binding.star1, binding.star2, binding.star3, binding.star4, binding.star5};
        for (int i = 0; i < stars.length; i++) {
            ((android.widget.ImageButton) stars[i]).setImageResource(
                    i < selectedStars ? R.drawable.ic_night_star
                            : R.drawable.ic_night_star_border);
        }
    }

    private void loopPulse(View target, float from, float to, long periodMs) {
        unbindPulse(target);
        android.animation.ValueAnimator animator =
                android.animation.ValueAnimator.ofFloat(from, to);
        animator.setDuration(periodMs);
        animator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        animator.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        animator.addUpdateListener(a -> {
            float v = (float) a.getAnimatedValue();
            target.setScaleX(v);
            target.setScaleY(v);
        });
        target.setTag(animator);
        animator.start();
    }

    private void navigateToMain(int destinationId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_NAV_DESTINATION, destinationId);
        // CLEAR_TOP|SINGLE_TOP redelivers the Intent through onNewIntent so the
        // destination extra actually reaches MainActivity.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finishWithSlide();
    }

    private void finishWithSlide() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.night_activity_exit);
    }

    private void stopEtaTimer() {
        if (etaTimer != null) {
            etaTimer.cancel();
            etaTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.nightMap.start();
    }

    @Override
    protected void onPause() {
        binding.nightMap.stop();
        super.onPause();
    }

    /** Back handling via OnBackPressedDispatcher (works on API 33+ predictive back). */
    private void handleBack() {
        NightOpsStateMachine.Phase phase = machine.phase();
        boolean rideActive = phase == NightOpsStateMachine.Phase.RIDE_CONFIRMED
                || phase == NightOpsStateMachine.Phase.DRIVER_ARRIVING
                || phase == NightOpsStateMachine.Phase.DRIVER_ARRIVED;
        if (sosActive) {
            cancelSos();
        } else if (rideActive) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.night_ops_cancel_title))
                    .setMessage(getString(R.string.night_ops_cancel_msg))
                    .setPositiveButton(getString(R.string.night_ops_cancel_yes),
                            (d, w) -> {
                                machine.fire(NightOpsStateMachine.Event.CANCEL_RIDE);
                                Toast.makeText(NightOpsActivity.this,
                                        getString(R.string.night_ops_cancelled),
                                        Toast.LENGTH_SHORT).show();
                                finishWithSlide();
                            })
                    .setNegativeButton(getString(R.string.night_ops_cancel_no), null)
                    .show();
        } else {
            finishWithSlide();
        }
    }

    @Override
    protected void onDestroy() {
        stopEtaTimer();
        handler.removeCallbacksAndMessages(null);
        unbindPulse(binding.gpsDot);
        unbindPulse(binding.bannerDotPing);
        unbindPulse(binding.otpCard);
        unbindPulse(binding.sosRing);
        if (otpDialog != null && otpDialog.isShowing()) {
            otpDialog.dismiss();
        }
        otpDialog = null;
        super.onDestroy();
    }

    private void unbindPulse(View target) {
        if (target != null && target.getTag() instanceof android.animation.ValueAnimator) {
            ((android.animation.ValueAnimator) target.getTag()).cancel();
            target.setTag(null);
        }
    }
}
