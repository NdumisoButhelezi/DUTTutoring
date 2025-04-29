package com.example.dutpeertutoring;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Random;

public class BubbleView extends View {
    private ArrayList<Bubble> bubbles;
    private Paint paint;
    private Random random;
    private int width, height;
    private static final int NUM_BUBBLES = 20;
    private int primaryColor;

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bubbles = new ArrayList<>();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        primaryColor = ContextCompat.getColor(getContext(), R.color.md_theme_primary);
        paint.setColor(primaryColor);
        paint.setAlpha(50); // Semi-transparent bubbles
        random = new Random();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        initializeBubbles();
    }

    private void initializeBubbles() {
        bubbles.clear();
        for (int i = 0; i < NUM_BUBBLES; i++) {
            createNewBubble();
        }
    }

    private void createNewBubble() {
        float size = random.nextFloat() * 60 + 30; // Larger bubbles for Material Design
        float x = random.nextFloat() * width;
        float y = -size;
        float speed = random.nextFloat() * 3 + 1; // Slower, more elegant movement
        bubbles.add(new Bubble(x, y, size, speed));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < bubbles.size(); i++) {
            Bubble bubble = bubbles.get(i);
            bubble.y += bubble.speed;
            bubble.x += Math.sin(bubble.y / 50) * 1.5; // Gentler wobble

            // Draw bubble with subtle gradient
            paint.setAlpha((int)(50 + (Math.sin(bubble.y / 30) + 1) * 25));
            canvas.drawCircle(bubble.x, bubble.y, bubble.size, paint);

            if (bubble.y > height + bubble.size) {
                bubble.y = -bubble.size;
                bubble.x = random.nextFloat() * width;
            }
        }

        invalidate();
    }

    private static class Bubble {
        float x, y, size, speed;

        Bubble(float x, float y, float size, float speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
        }
    }
}