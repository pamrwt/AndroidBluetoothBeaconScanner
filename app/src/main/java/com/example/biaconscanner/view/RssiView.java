package com.example.biaconscanner.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.view.View;

import com.example.biaconscanner.R;

public class RssiView extends View {
    private int mRssi;
    private Paint paintBorder;
    private Paint paintProgress;
    private Paint paintProgressBackground;
    private Paint paintTextInvert;
    private Paint paintTextWhite;
    private Rect textBounds = new Rect();

    public RssiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float textSize = getContext().getResources().getDimension(R.dimen.alert_dialog_padding);
        int progressColor = context.getResources().getColor(R.color.colorPrimary);
        this.paintBorder = new Paint();
        this.paintBorder.setColor(progressColor);
        this.paintBorder.setStrokeWidth(1.0f);
        this.paintBorder.setStyle(Style.STROKE);
        this.paintProgress = new Paint();
        this.paintProgress.setColor(progressColor);
        this.paintProgress.setStyle(Style.FILL);
        this.paintProgressBackground = new Paint();
        this.paintProgressBackground.setColor(-1);
        this.paintProgressBackground.setStyle(Style.FILL);
        this.paintTextWhite = new Paint();
        this.paintTextWhite.setColor(-1);
        this.paintTextWhite.setStrokeWidth(1.0f);
        this.paintTextWhite.setTextSize(textSize);
        this.paintTextWhite.setAntiAlias(true);
        this.paintTextWhite.setDither(true);
        this.paintTextInvert = new Paint();
        this.paintTextInvert.setColor(progressColor);
        this.paintTextInvert.setStrokeWidth(1.0f);
        this.paintTextInvert.setTextSize(textSize);
        this.paintTextInvert.setAntiAlias(true);
        this.paintTextInvert.setDither(true);
    }

    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), (int) Math.max((float) getMeasuredHeight(), getContext().getResources().getDimension(R.dimen.alert_dialog_padding)));
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mRssi < 0) {
            canvas.drawRect(0.0f, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight(), this.paintProgressBackground);
            float rssiFactor = ((float) (this.mRssi + 100)) / 100.0f;
            canvas.drawRect(0.0f, 0.0f, ((float) getMeasuredWidth()) * rssiFactor, (float) getMeasuredHeight(), this.paintProgress);
            String rssiText = String.valueOf(this.mRssi);
            this.paintTextWhite.getTextBounds(rssiText, 0, rssiText.length(), this.textBounds);
            canvas.clipRect(0.0f, 0.0f, ((float) getMeasuredWidth()) * rssiFactor, (float) getMeasuredHeight());
            canvas.drawText(rssiText, ((float) (getMeasuredWidth() / 2)) - this.textBounds.exactCenterX(), ((float) (getMeasuredHeight() / 2)) - this.textBounds.exactCenterY(), this.paintTextWhite);
            canvas.clipRect(0.0f, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight(), Op.INTERSECT);
            canvas.drawText(rssiText, ((float) (getMeasuredWidth() / 2)) - this.textBounds.exactCenterX(), ((float) (getMeasuredHeight() / 2)) - this.textBounds.exactCenterY(), this.paintTextInvert);
        }
        canvas.drawRect(0.0f, 0.0f, (float) (getMeasuredWidth() - 1), (float) (getMeasuredHeight() - 1), this.paintBorder);
    }
}
