package com.oncab.user.ui.nightops;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.oncab.user.R;

/**
 * Night cartography canvas — a faithful port of the approved proto3 map SVG:
 * dark base, Hooghly river ribbon, Kolkata street grid, Maidan green block,
 * glowing amber route (Park St → Esplanade via Chowringhee), pulsing pickup
 * pin, drop pin, nearby cab dots and a live cab with headlight beams driving
 * the route. All geometry is normalized from the proto viewBox (400 x 270).
 */
public class NightMapView extends View {

    // Normalized anchor points (viewBox 400x270 from the approved design)
    private static final float[][] ROUTE = {
            {255f / 400f, 225f / 270f},   // Park St pickup
            {215f / 400f, 190f / 270f},
            {185f / 400f, 130f / 270f},   // Chowringhee bend
            {195f / 400f, 65f / 270f}     // Esplanade drop
    };
    private static final float[][] NEARBY_CABS = {
            {140f / 400f, 110f / 270f},
            {290f / 400f, 85f / 270f},
            {295f / 400f, 205f / 270f}
    };
    private static final float PICKUP_X = 195f / 400f, PICKUP_Y = 65f / 270f;
    private static final float DROP_X = 255f / 400f, DROP_Y = 225f / 270f;

    private static final long PING_PERIOD_MS = 1600L;
    private static final long CAB_PERIOD_MS = 9000L;

    private final Paint paintBase = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRiver = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRiverStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRoad = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLabelDim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRouteGlow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRoute = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintPin = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintPinStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintPing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintChip = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintChipStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintChipText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCab = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCabStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintBeam = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintVignette = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path riverPath = new Path();
    private final Path routePath = new Path();
    private final Path beamPath = new Path();
    private final PathMeasure routeMeasure = new PathMeasure();
    private final RectF tmpRect = new RectF();
    private final float[] cabPos = new float[2];
    private final float[] cabTan = new float[2];
    private DashPathEffect minorDash;

    private int colorBase, colorRiver, colorRiverStroke, colorMaidan, colorMaidanStroke;
    private int colorAmber, colorGreen, colorGreenText, colorGreenOn, colorDark;
    private int colorChipBg, colorLabel, colorLabelDim, colorRiverLabel;
    private int colorAmberSoft, colorCabCore, colorCabRing;
    private int roadMinor, roadMid, roadMain, roadBroad, roadThin;
    private String labelRiver, labelEsplanadeCr, labelParkStreet, labelCamacSt,
            labelMaidanGreen, chipPickup, chipEsplanade;
    private float pingPhase;      // 0..1 pulse of the pickup pin
    private float cabProgress;    // 0..1 along the route
    private ValueAnimator pingAnimator;
    private ValueAnimator cabAnimator;

    public NightMapView(Context context) {
        super(context);
        init();
    }

    public NightMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NightMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Context c = getContext();
        colorBase = ContextCompat.getColor(c, R.color.night_surface_lowest);
        colorRiver = ContextCompat.getColor(c, R.color.night_river);
        colorRiverStroke = ContextCompat.getColor(c, R.color.night_river_stroke);
        colorMaidan = ContextCompat.getColor(c, R.color.night_maidan);
        colorMaidanStroke = ContextCompat.getColor(c, R.color.night_maidan_stroke);
        colorAmber = ContextCompat.getColor(c, R.color.night_amber);
        colorGreen = ContextCompat.getColor(c, R.color.night_green);
        colorGreenText = ContextCompat.getColor(c, R.color.night_green_text);
        colorGreenOn = ContextCompat.getColor(c, R.color.night_green_on);
        colorDark = ContextCompat.getColor(c, R.color.night_surface_lowest);
        colorChipBg = ContextCompat.getColor(c, R.color.night_surface_low);
        colorLabel = ContextCompat.getColor(c, R.color.night_map_label);
        colorLabelDim = ContextCompat.getColor(c, R.color.night_map_label_dim);
        colorRiverLabel = ContextCompat.getColor(c, R.color.night_river_label);
        colorAmberSoft = ContextCompat.getColor(c, R.color.night_amber_soft);
        colorCabCore = ContextCompat.getColor(c, R.color.night_map_cab_core);
        colorCabRing = ContextCompat.getColor(c, R.color.night_map_cab_ring);
        roadMinor = ContextCompat.getColor(c, R.color.night_road_minor);
        roadMid = ContextCompat.getColor(c, R.color.night_road_mid);
        roadMain = ContextCompat.getColor(c, R.color.night_road_main);
        roadBroad = ContextCompat.getColor(c, R.color.night_road_broad);
        roadThin = ContextCompat.getColor(c, R.color.night_road_thin);
        // Canvas map labels are cartography glyphs, loaded from resources
        labelRiver = c.getString(R.string.night_ops_map_river);
        labelEsplanadeCr = c.getString(R.string.night_ops_map_esplanade_cr);
        labelParkStreet = c.getString(R.string.night_ops_map_park_street);
        labelCamacSt = c.getString(R.string.night_ops_map_camac_st);
        labelMaidanGreen = c.getString(R.string.night_ops_map_maidan_green);
        chipPickup = c.getString(R.string.night_ops_map_chip_pickup);
        chipEsplanade = c.getString(R.string.night_ops_map_chip_esplanade);

        paintBase.setColor(colorBase);
        paintRiver.setColor(colorRiver);
        paintRiverStroke.setColor(colorRiverStroke);
        paintRiverStroke.setStrokeWidth(1.5f);
        paintRiverStroke.setStyle(Paint.Style.STROKE);

        paintRoad.setStyle(Paint.Style.STROKE);
        paintRoad.setStrokeCap(Paint.Cap.ROUND);

        paintRoute.setStyle(Paint.Style.STROKE);
        paintRoute.setStrokeCap(Paint.Cap.ROUND);
        paintRoute.setStrokeJoin(Paint.Join.ROUND);
        paintRouteGlow.setStyle(Paint.Style.STROKE);
        paintRouteGlow.setStrokeCap(Paint.Cap.ROUND);
        paintRouteGlow.setStrokeJoin(Paint.Join.ROUND);

        paintLabel.setColor(colorLabel);
        paintLabelDim.setColor(colorLabelDim);
        paintLabel.setTextSize(9f);
        paintLabelDim.setTextSize(8f);
        paintLabel.setLetterSpacing(0.08f);
        paintLabelDim.setLetterSpacing(0.08f);
        paintLabel.setFakeBoldText(true);
        paintLabelDim.setFakeBoldText(false);

        paintPing.setColor(colorGreen);
        paintPin.setColor(colorGreen);
        paintPinStroke.setColor(colorGreenOn);
        paintPinStroke.setStyle(Paint.Style.STROKE);

        paintChip.setColor(colorChipBg);
        paintChipStroke.setStyle(Paint.Style.STROKE);
        paintChipText.setColor(colorGreenText);
        paintChipText.setFakeBoldText(true);

        paintCab.setColor(colorAmber);
        paintCabStroke.setColor(colorDark);
        paintCabStroke.setStyle(Paint.Style.STROKE);
        paintBeam.setColor(Color.WHITE);
        paintBeam.setAlpha(51);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) {
            return;
        }
        // River ribbon (left edge, curved like the proto)
        riverPath.reset();
        riverPath.moveTo(-0.025f * w, 0);
        riverPath.cubicTo(0.0625f * w, 0.259f * h, 0.0375f * w, 0.592f * h, -0.025f * w, h);
        riverPath.close();

        // Glowing route
        routePath.reset();
        routePath.moveTo(ROUTE[0][0] * w, ROUTE[0][1] * h);
        for (int i = 1; i < ROUTE.length; i++) {
            routePath.lineTo(ROUTE[i][0] * w, ROUTE[i][1] * h);
        }
        routeMeasure.setPath(routePath, false);

        float s = Math.min(w, h) / 270f; // stroke scale anchored to design height
        minorDash = new DashPathEffect(new float[]{4f * s, 3f * s}, 0);
        paintRiverStroke.setStrokeWidth(1.5f * s);
        paintRoute.setStrokeWidth(3.5f * s);
        paintRouteGlow.setStrokeWidth(8f * s);
        paintRouteGlow.setColor(colorAmber);
        paintRouteGlow.setAlpha(102);
        paintChipText.setTextSize(9f * s * 1.35f);
        paintLabel.setTextSize(9f * s * 1.35f);
        paintLabelDim.setTextSize(8f * s * 1.35f);

        Shader vignette = new RadialGradient(0.55f * w, 0.45f * h, 0.75f * Math.max(w, h),
                new int[]{Color.TRANSPARENT, (colorBase & 0x00FFFFFF) | 0xCC000000},
                new float[]{0.6f, 1f}, Shader.TileMode.CLAMP);
        paintVignette.setShader(vignette);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float s = Math.min(w, h) / 270f;
        canvas.drawColor(colorBase);

        // River
        canvas.drawPath(riverPath, paintRiver);
        canvas.drawPath(riverPath, paintRiverStroke);
        canvas.save();
        canvas.rotate(-90, 0.015f * w, 0.52f * h);
        paintLabelDim.setColor(colorRiverLabel);
        canvas.drawText(labelRiver, 0.015f * w, 0.52f * h, paintLabelDim);
        paintLabelDim.setColor(colorLabelDim);
        canvas.restore();

        // Street grid (horizontal, then avenues)
        road(canvas, -0.05f, 60f / 270f, 1.05f, 50f / 270f, 6f * s, roadMid);
        road(canvas, -0.05f, 135f / 270f, 1.05f, 120f / 270f, 5f * s, roadMain);
        road(canvas, -0.05f, 210f / 270f, 1.05f, 215f / 270f, 7f * s, roadMid);
        road(canvas, 90f / 400f, -0.04f, 70f / 400f, 1.04f, 5f * s, roadMid);
        road(canvas, 190f / 400f, -0.04f, 180f / 400f, 1.04f, 8f * s, roadBroad);
        road(canvas, 290f / 400f, -0.04f, 310f / 400f, 1.04f, 6f * s, roadMain);
        road(canvas, 370f / 400f, -0.04f, 350f / 400f, 1.04f, 4f * s, roadThin);
        // Minor connectors (dashed like the proto)
        paintRoad.setColor(roadMinor);
        paintRoad.setStrokeWidth(3f * s);
        paintRoad.setPathEffect(minorDash);
        canvas.drawLine(40f / 400f * w, 180f / 270f * h, 180f / 400f * w, 135f / 270f * h, paintRoad);
        canvas.drawLine(180f / 400f * w, 135f / 270f * h, 290f / 400f * w, 170f / 270f * h, paintRoad);
        paintRoad.setPathEffect(null);
        canvas.drawLine(180f / 400f * w, 70f / 270f * h, 350f / 400f * w, 110f / 270f * h, paintRoad);

        // Maidan green zone
        tmpRect.set(78f / 400f * w, 102f / 270f * h, 148f / 400f * w, 192f / 270f * h);
        paintChip.setStrokeWidth(1f * s);
        paintChip.setColor(colorMaidan);
        paintChip.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(tmpRect, 6f * s, 6f * s, paintChip);
        paintChip.setColor(colorMaidanStroke);
        paintChip.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(tmpRect, 6f * s, 6f * s, paintChip);
        paintChip.setStyle(Paint.Style.FILL);

        // Landmark labels
        paintLabel.setColor(colorLabel);
        canvas.drawText(labelEsplanadeCr, 185f / 400f * w, 32f / 270f * h, paintLabel);
        canvas.drawText(labelParkStreet, 205f / 400f * w, 240f / 270f * h, paintLabel);
        paintLabelDim.setColor(colorLabelDim);
        canvas.drawText(labelCamacSt, 315f / 400f * w, 160f / 270f * h, paintLabelDim);
        canvas.drawText(labelMaidanGreen, 80f / 400f * w, 95f / 270f * h, paintLabelDim);

        // Route glow + sharp foreground
        canvas.drawPath(routePath, paintRouteGlow);
        canvas.drawPath(routePath, paintRoute);

        // Nearby night cabs (amber dots)
        for (float[] p : NEARBY_CABS) {
            float cx = p[0] * w, cy = p[1] * h;
            paintPin.setColor(colorChipBg);
            canvas.drawCircle(cx, cy, 4.5f * s, paintPin);
            paintPin.setColor(colorCabRing);
            paintPin.setStyle(Paint.Style.STROKE);
            paintPin.setStrokeWidth(1.5f * s);
            canvas.drawCircle(cx, cy, 4.5f * s, paintPin);
            paintPin.setStyle(Paint.Style.FILL);
            paintPin.setColor(colorCabCore);
            canvas.drawCircle(cx, cy, 1.5f * s, paintPin);
        }

        // Drop pin (Esplanade, amber)
        float dx = DROP_X * w, dy = DROP_Y * h;
        paintPin.setColor(colorAmber);
        paintPin.setAlpha(38);
        canvas.drawCircle(dx, dy, 12f * s, paintPin);
        paintPin.setAlpha(255);
        paintPin.setColor(colorDark);
        canvas.drawCircle(dx, dy, 6f * s, paintPin);
        paintPin.setStyle(Paint.Style.STROKE);
        paintPin.setStrokeWidth(2.5f * s);
        paintPin.setColor(colorAmber);
        canvas.drawCircle(dx, dy, 6f * s, paintPin);
        paintPin.setStyle(Paint.Style.FILL);
        drawChip(canvas, dx - 10f * s - 62f * s * 1.35f, dy, chipEsplanade, colorLabel, colorAmberSoft);

        // Pickup pin (Park St, green, pulsing)
        float px = PICKUP_X * w, py = PICKUP_Y * h;
        float pingR = (14f + 10f * pingPhase) * s;
        paintPing.setAlpha((int) (51 * (1f - pingPhase)));
        canvas.drawCircle(px, py, pingR, paintPing);
        paintPing.setAlpha(255);
        paintPin.setColor(colorGreen);
        paintPin.setAlpha(51);
        canvas.drawCircle(px, py, 14f * s, paintPin);
        paintPin.setAlpha(255);
        paintPin.setColor(colorGreen);
        canvas.drawCircle(px, py, 7f * s, paintPin);
        paintPin.setStyle(Paint.Style.STROKE);
        paintPinStroke.setStrokeWidth(2f * s);
        canvas.drawCircle(px, py, 7f * s, paintPinStroke);
        paintPin.setStyle(Paint.Style.FILL);
        paintPin.setColor(Color.WHITE);
        canvas.drawCircle(px, py, 2.5f * s, paintPin);
        drawChip(canvas, px + 10f * s, py, chipPickup, colorGreen, colorGreenText);

        // Live cab driving the route
        if (routeMeasure.getLength() > 0) {
            float dist = cabProgress * routeMeasure.getLength();
            routeMeasure.getPosTan(dist, cabPos, cabTan);
            canvas.save();
            canvas.translate(cabPos[0], cabPos[1]);
            canvas.rotate((float) Math.toDegrees(Math.atan2(cabTan[1], cabTan[0])));
            paintCab.setColor(colorAmber);
            paintCab.setAlpha(64);
            canvas.drawCircle(0, 0, 16f * s, paintCab);
            paintCab.setAlpha(255);
            paintBeam.setColor(Color.WHITE);
            paintBeam.setAlpha(51);
            beamPath.reset();
            beamPath.moveTo(13f * s, -7f * s);
            beamPath.lineTo(30f * s, -13f * s);
            beamPath.lineTo(30f * s, 13f * s);
            beamPath.lineTo(13f * s, 7f * s);
            beamPath.close();
            canvas.drawPath(beamPath, paintBeam);
            tmpRect.set(-13f * s, -9f * s, 13f * s, 9f * s);
            canvas.drawRoundRect(tmpRect, 4f * s, 4f * s, paintCab);
            paintCabStroke.setStrokeWidth(2f * s);
            canvas.drawRoundRect(tmpRect, 4f * s, 4f * s, paintCabStroke);
            paintCab.setColor(colorDark);
            canvas.drawRoundRect(-4f * s, -2f * s, 4f * s, 2f * s, 1f * s, 1f * s, paintCab);
            canvas.restore();
        }

        canvas.drawRect(0, 0, w, h, paintVignette);
    }

    private void road(Canvas canvas, float x1, float y1, float x2, float y2, float width, int color) {
        paintRoad.setColor(color);
        paintRoad.setStrokeWidth(width);
        canvas.drawLine(x1 * getWidth(), y1 * getHeight(), x2 * getWidth(), y2 * getHeight(), paintRoad);
    }

    private void drawChip(Canvas canvas, float left, float centerY, String text, int strokeColor,
                          int textColor) {
        float pad = 5f;
        float tw = paintChipText.measureText(text);
        float th = paintChipText.getTextSize();
        float h = th + 2 * pad;
        float w = tw + 2 * pad;
        tmpRect.set(left, centerY - h / 2f, left + w, centerY + h / 2f);
        paintChip.setColor(colorChipBg);
        canvas.drawRoundRect(tmpRect, 4f, 4f, paintChip);
        paintChipStroke.setColor(strokeColor);
        paintChipStroke.setStrokeWidth(1f);
        canvas.drawRoundRect(tmpRect, 4f, 4f, paintChipStroke);
        paintChipText.setColor(textColor);
        canvas.drawText(text, left + pad, centerY + th * 0.35f, paintChipText);
    }

    /** Starts the ping pulse + live-cab animations. Safe to call twice. */
    public void start() {
        if (pingAnimator == null) {
            pingAnimator = ValueAnimator.ofFloat(0f, 1f);
            pingAnimator.setDuration(PING_PERIOD_MS);
            pingAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pingAnimator.setInterpolator(new LinearInterpolator());
            pingAnimator.addUpdateListener(a -> {
                pingPhase = (float) a.getAnimatedValue();
                invalidate();
            });
            pingAnimator.start();
        }
        if (cabAnimator == null) {
            cabAnimator = ValueAnimator.ofFloat(0f, 1f);
            cabAnimator.setDuration(CAB_PERIOD_MS);
            cabAnimator.setRepeatCount(ValueAnimator.INFINITE);
            cabAnimator.setRepeatMode(ValueAnimator.REVERSE);
            cabAnimator.setInterpolator(new LinearInterpolator());
            cabAnimator.addUpdateListener(a -> {
                cabProgress = (float) a.getAnimatedValue();
                invalidate();
            });
            cabAnimator.start();
        }
    }

    /** Stops animations (activity pause). */
    public void stop() {
        if (pingAnimator != null) {
            pingAnimator.cancel();
            pingAnimator = null;
        }
        if (cabAnimator != null) {
            cabAnimator.cancel();
            cabAnimator = null;
        }
    }

    /** Snaps the live cab back to the pickup end (recenter action). */
    public void recenter() {
        if (cabAnimator != null) {
            cabAnimator.cancel();
        }
        cabAnimator = ValueAnimator.ofFloat(0f, 1f);
        cabAnimator.setDuration(CAB_PERIOD_MS);
        cabAnimator.setRepeatCount(ValueAnimator.INFINITE);
        cabAnimator.setRepeatMode(ValueAnimator.REVERSE);
        cabAnimator.setInterpolator(new LinearInterpolator());
        cabAnimator.addUpdateListener(a -> {
            cabProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        cabAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }
}
