package com.example.rise_of_city.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom View để vẽ đường thẳng nối từ node này đến node tiếp theo
 */
public class ConnectorLineView extends View {
    private Paint paint;
    private Path path;
    private int lineColor = 0xFFB2EBF2; // Light blue
    private float startX = 0.5f; // 0.0 = left, 1.0 = right
    private float startY = 1.0f; // 0.0 = top, 1.0 = bottom
    private float endX = 0.5f;
    private float endY = 0.0f;
    private float lineWidth = 4f;
    
    public ConnectorLineView(Context context) {
        super(context);
        init();
    }
    
    public ConnectorLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ConnectorLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(lineColor);
        paint.setStrokeWidth(lineWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        
        path = new Path();
    }
    
    /**
     * Set điểm bắt đầu (relative: 0.0-1.0)
     */
    public void setStartPoint(float x, float y) {
        startX = x;
        startY = y;
        invalidate();
    }
    
    /**
     * Set điểm kết thúc (relative: 0.0-1.0)
     */
    public void setEndPoint(float x, float y) {
        endX = x;
        endY = y;
        invalidate();
    }
    
    /**
     * Set màu đường thẳng
     */
    public void setLineColor(int color) {
        lineColor = color;
        paint.setColor(color);
        invalidate();
    }
    
    /**
     * Set độ dày đường thẳng
     */
    public void setLineWidth(float width) {
        lineWidth = width;
        paint.setStrokeWidth(width);
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        
        // Tính toán tọa độ thực tế
        // Cho phép vẽ vượt quá bounds để line có thể nối đến item tiếp theo
        float startXPos = startX * getWidth();
        float startYPos = startY * getHeight();
        float endXPos = endX * getWidth();
        float endYPos = endY * getHeight();
        
        // Vẽ đường thẳng từ điểm bắt đầu đến điểm kết thúc
        // Line có thể vượt quá bounds của view để nối đến item tiếp theo
        path.reset();
        path.moveTo(startXPos, startYPos);
        path.lineTo(endXPos, endYPos);
        
        canvas.drawPath(path, paint);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Cho phép view vẽ vượt quá bounds nếu cần
        setWillNotDraw(false);
    }
}

