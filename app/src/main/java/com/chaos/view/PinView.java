package com.chaos.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import com.oncab.user.R;

/**
 * PinView — vendored port of com.chaos.view:pinview:1.4.4 (jcenter artifact no longer
 * resolvable). Supports the exact attr subset used by OnCab layouts:
 * itemCount, itemWidth, itemHeight, itemRadius, itemSpacing, lineColor, lineWidth,
 * viewType(rectangle|line|none), cursorColor, cursorWidth, hideLineWhenFilled.
 */
public class PinView extends AppCompatEditText {

    public static final int VIEW_TYPE_RECTANGLE = 0;
    public static final int VIEW_TYPE_LINE = 1;
    public static final int VIEW_TYPE_NONE = 2;

    private int itemCount = 4;
    private int itemWidth = 48;
    private int itemHeight = 56;
    private int itemRadius = 0;
    private int itemSpacing = 12;
    private int lineColor = Color.BLACK;
    private int lineWidth = 1;
    private int viewType = VIEW_TYPE_RECTANGLE;
    private int cursorColor = Color.BLACK;
    private int cursorWidth = 2;
    private boolean hideLineWhenFilled = false;
    private boolean animationEnable = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private int boxWidth;
    private int boxHeight;

    public PinView(Context context) {
        super(context);
        init(null);
    }

    public PinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setBackgroundResource(android.R.color.transparent);
        setFocusableInTouchMode(true);
        setTextIsSelectable(false);
        setMaxLines(1);
        setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(itemCount)});
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.PinView);
            itemCount = ta.getInt(R.styleable.PinView_itemCount, itemCount);
            itemWidth = ta.getDimensionPixelSize(R.styleable.PinView_itemWidth, itemWidth);
            itemHeight = ta.getDimensionPixelSize(R.styleable.PinView_itemHeight, itemHeight);
            itemRadius = ta.getDimensionPixelSize(R.styleable.PinView_itemRadius, itemRadius);
            itemSpacing = ta.getDimensionPixelSize(R.styleable.PinView_itemSpacing, itemSpacing);
            lineColor = ta.getColor(R.styleable.PinView_lineColor, lineColor);
            lineWidth = ta.getDimensionPixelSize(R.styleable.PinView_lineWidth, lineWidth);
            viewType = ta.getInt(R.styleable.PinView_viewType, viewType);
            cursorColor = ta.getColor(R.styleable.PinView_cursorColor, cursorColor);
            cursorWidth = ta.getDimensionPixelSize(R.styleable.PinView_cursorWidth, cursorWidth);
            hideLineWhenFilled = ta.getBoolean(R.styleable.PinView_hideLineWhenFilled, hideLineWhenFilled);
            animationEnable = ta.getBoolean(R.styleable.PinView_animationEnable, false);
            ta.recycle();
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
    }

    public void setAnimationEnable(boolean enable) {
        this.animationEnable = enable;
    }

    public void setLineColor(int color) {
        this.lineColor = color;
        invalidate();
    }

    public void setCursorColor(int color) {
        this.cursorColor = color;
        invalidate();
    }

    public void setItemCount(int count) {
        this.itemCount = count;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boxWidth = itemWidth;
        boxHeight = itemHeight;
        int w = itemCount * boxWidth + (itemCount - 1) * itemSpacing
                + getPaddingLeft() + getPaddingRight();
        int h = boxHeight + getPaddingTop() + getPaddingBottom();
        int wm = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY);
        int hm = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY);
        super.onMeasure(wm, hm);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int textLength = getText() == null ? 0 : getText().length();
        int textPad = getPaddingTop();
        int textSize = (int) getTextSize();
        int cursorHalf = Math.max(1, cursorWidth / 2);

        for (int i = 0; i < itemCount; i++) {
            int left = getPaddingLeft() + i * (boxWidth + itemSpacing);
            int top = getPaddingTop();
            int right = left + boxWidth;
            int bottom = top + boxHeight;
            char c = i < textLength ? getText().charAt(i) : 0;

            boolean filled = c != 0;
            if (viewType == VIEW_TYPE_RECTANGLE) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(lineColor);
                paint.setStrokeWidth(lineWidth);
                rect.set(left, top, right, bottom);
                canvas.drawRoundRect(rect, itemRadius, itemRadius, paint);
            } else if (viewType == VIEW_TYPE_LINE) {
                if (!(hideLineWhenFilled && filled)) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(lineColor);
                    paint.setStrokeWidth(lineWidth);
                    float y = bottom - lineWidth / 2f;
                    canvas.drawLine(left, y, right, y, paint);
                }
            }

            if (filled) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(getCurrentTextColor());
                paint.setTextSize(textSize);
                paint.setTextAlign(Paint.Align.CENTER);
                Paint.FontMetrics fm = paint.getFontMetrics();
                float baseline = top + (boxHeight - fm.ascent - fm.descent) / 2f - fm.descent;
                canvas.drawText(String.valueOf(c), left + boxWidth / 2f, baseline, paint);
            } else if (hasFocus() && getSelectionStart() == i && isCursorVisible()) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(cursorColor);
                float cx = left + boxWidth / 2f;
                canvas.drawRect(cx - cursorHalf, top + textPad,
                        cx + cursorHalf, bottom - textPad, paint);
            }
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (getText() != null && selEnd != getText().length()) {
            setSelection(getText().length());
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override
    public void setBackground(Drawable background) {
        // keep transparent background; boxes are drawn manually
    }
}
