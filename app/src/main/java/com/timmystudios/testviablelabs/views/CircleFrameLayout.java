package com.timmystudios.testviablelabs.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CircleFrameLayout extends FrameLayout {

    private Path path;
    private Paint paint;

    public CircleFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public CircleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        setWillNotDraw(false);
        path = new Path();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int halfWidth = w / 2;
        int halfHeight = h / 2;
        int radius;
        if (w <= h) {
            radius = halfWidth;
        } else {
            radius = halfHeight;
        }
        path.reset();
        path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CW);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawPath(path, paint);
    }
}
