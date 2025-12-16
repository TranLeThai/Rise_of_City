package com.example.rise_of_city.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom View để vẽ đường nối zigzag giữa các building nodes
 */
public class ZigzagConnectorView extends View {
    private Paint paint;
    private Path path;
    private int lineColor = 0xFFB2EBF2; // Màu xanh mặc định
    private float startX, startY, endX, endY;
    private boolean isVisible = true;

    public ZigzagConnectorView(Context context) {
        super(context);
        init();
    }

    public ZigzagConnectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZigzagConnectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(lineColor);
        
        path = new Path();
    }

    /**
     * Thiết lập điểm bắt đầu và kết thúc của đường nối
     */
    public void setConnectionPoints(float startX, float startY, float endX, float endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        invalidate();
    }

    /**
     * Thiết lập màu của đường nối
     */
    public void setLineColor(int color) {
        this.lineColor = color;
        paint.setColor(color);
        invalidate();
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
        setVisibility(visible ? VISIBLE : GONE);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isVisible) {
            return;
        }

        // Vẽ đường thẳng nối từ start đến end
        path.reset();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Đảm bảo view có đủ không gian để vẽ đường nối
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}

