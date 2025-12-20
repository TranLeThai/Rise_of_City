package com.example.rise_of_city.ui.matching;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom View để vẽ các đường nối giữa các từ đã match
 */
public class MatchingLineView extends View {
    
    private Paint linePaint;
    private List<LineConnection> connections;
    
    public MatchingLineView(Context context) {
        super(context);
        init();
    }
    
    public MatchingLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        linePaint = new Paint();
        linePaint.setColor(0xFF000000);
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        
        connections = new ArrayList<>();
    }
    
    public void addConnection(float startX, float startY, float endX, float endY) {
        connections.add(new LineConnection(startX, startY, endX, endY));
        invalidate();
    }
    
    public void clearConnections() {
        connections.clear();
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (LineConnection connection : connections) {
            Path path = new Path();
            path.moveTo(connection.startX, connection.startY);
            path.lineTo(connection.endX, connection.endY);
            canvas.drawPath(path, linePaint);
        }
    }
    
    private static class LineConnection {
        float startX, startY, endX, endY;
        
        LineConnection(float startX, float startY, float endX, float endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }
    }
}
