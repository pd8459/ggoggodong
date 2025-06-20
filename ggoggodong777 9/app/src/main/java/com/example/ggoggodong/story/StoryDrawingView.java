package com.example.ggoggodong.story;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.ggoggodong.model.Line;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StoryDrawingView extends View {
    private Paint paint;
    private Path currentPath;
    private List<Path> paths;
    private List<Integer> colors;
    private int currentColor;
    private boolean isEraseMode;
    private float eraseRadius = 30f;

    private Bitmap backgroundBitmap;
    private Paint backgroundPaint = new Paint();

    private List<Sticker> stickers = new ArrayList<>();
    private Sticker selectedSticker = null;
    private float lastTouchX, lastTouchY;
    private boolean isInHandleMode = false;

    private float initialAngle = 0f;
    private float initialDistance = 0f;
    private float initialRotation = 0f;
    private float initialScale = 1.0f;

    private boolean isStickerMode = true;

    public interface OnStrokeCompleteListener {
        void onStrokeComplete(Line line);
    }
    private OnStrokeCompleteListener listener;

    public void setOnStrokeCompleteListener(OnStrokeCompleteListener l) {
        listener = l;
    }

    public StoryDrawingView(Context context) {
        super(context);
        init();
    }

    public StoryDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(8f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        paths = new ArrayList<>();
        colors = new ArrayList<>();
        currentPath = new Path();
        currentColor = Color.BLACK;
        isEraseMode = false;
    }

    public void setStickerMode(boolean enabled) {
        isStickerMode = enabled;
    }

    public void addSticker(Bitmap bitmap, float x, float y) {
        stickers.add(new Sticker(bitmap, x, y));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, backgroundPaint);
        }

        for (Sticker sticker : stickers) {
            boolean isSelected = (sticker == selectedSticker);
            sticker.draw(canvas, isSelected);
        }

        for (int i = 0; i < paths.size(); i++) {
            paint.setColor(colors.get(i));
            canvas.drawPath(paths.get(i), paint);
        }

        if (!isEraseMode) {
            paint.setColor(currentColor);
            canvas.drawPath(currentPath, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (isStickerMode) {
                    selectedSticker = getTouchedSticker(x, y);
                } else {
                    selectedSticker = null;
                }

                if (isEraseMode) {
                    erasePath(x, y);
                    eraseSticker(x, y);
                } else if (selectedSticker != null) {
                    if (selectedSticker.isInHandle(x, y)) {
                        isInHandleMode = true;
                        float centerX = selectedSticker.getCenterX();
                        float centerY = selectedSticker.getCenterY();
                        float dxHandle = x - centerX;
                        float dyHandle = y - centerY;
                        initialAngle = (float) Math.toDegrees(Math.atan2(dyHandle, dxHandle));
                        initialDistance = (float) Math.hypot(dxHandle, dyHandle);
                        initialRotation = selectedSticker.rotation;
                        initialScale = selectedSticker.scale;
                    } else {
                        isInHandleMode = false;
                        lastTouchX = x;
                        lastTouchY = y;
                    }
                } else {
                    currentPath = new Path();
                    currentPath.moveTo(x, y);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isEraseMode) {
                    erasePath(x, y);
                    eraseSticker(x, y);
                } else if (selectedSticker != null) {
                    if (isInHandleMode) {
                        float centerX = selectedSticker.getCenterX();
                        float centerY = selectedSticker.getCenterY();
                        float dxMove = x - centerX;
                        float dyMove = y - centerY;
                        float currentAngle = (float) Math.toDegrees(Math.atan2(dyMove, dxMove));
                        float angleDiff = currentAngle - initialAngle;
                        float rotationResult = initialRotation + angleDiff;

                        float currentDistance = (float) Math.hypot(dxMove, dyMove);
                        float scaleFactor = currentDistance / initialDistance;
                        float newScale = initialScale * scaleFactor;

                        if (newScale < 0.05f) newScale = 0.05f;

                        selectedSticker.rotation = rotationResult;
                        selectedSticker.scale = newScale;
                    } else {
                        float dxMove = x - lastTouchX;
                        float dyMove = y - lastTouchY;
                        selectedSticker.x += dxMove;
                        selectedSticker.y += dyMove;
                        lastTouchX = x;
                        lastTouchY = y;
                    }
                } else {
                    currentPath.lineTo(x, y);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (!isEraseMode && selectedSticker == null) {
                    paths.add(currentPath);
                    colors.add(currentColor);
                }
                isInHandleMode = false;
                return true;
        }
        return false;
    }

    private void eraseSticker(float x, float y) {
        Iterator<Sticker> iterator = stickers.iterator();
        while (iterator.hasNext()) {
            Sticker sticker = iterator.next();
            if (sticker.contains(x, y)) {
                iterator.remove();
                break;
            }
        }
    }

    private Sticker getTouchedSticker(float x, float y) {
        for (int i = stickers.size() - 1; i >= 0; i--) {
            if (stickers.get(i).contains(x, y)) {
                Sticker touched = stickers.get(i);
                stickers.remove(i);
                stickers.add(touched);
                return touched;
            }
        }
        return null;
    }

    private static class Sticker {
        Bitmap bitmap;
        float x, y;
        float scale = 1.0f;
        float rotation = 0f;

        Sticker(Bitmap bitmap, float x, float y) {
            this.bitmap = bitmap;
            this.x = x;
            this.y = y;
        }

        float getCenterX() {
            return x + (bitmap.getWidth() * scale) / 2;
        }

        float getCenterY() {
            return y + (bitmap.getHeight() * scale) / 2;
        }

        boolean contains(float touchX, float touchY) {
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            matrix.postRotate(rotation, bitmap.getWidth() * scale / 2, bitmap.getHeight() * scale / 2);
            matrix.postTranslate(x, y);
            float[] pts = { touchX, touchY };
            Matrix inverse = new Matrix();
            if (matrix.invert(inverse)) {
                inverse.mapPoints(pts);
                return pts[0] >= 0 && pts[0] <= bitmap.getWidth() && pts[1] >= 0 && pts[1] <= bitmap.getHeight();
            }
            return false;
        }

        boolean isInHandle(float touchX, float touchY) {
            float width = bitmap.getWidth() * scale;
            float height = bitmap.getHeight() * scale;
            float handleSize = 40f;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            matrix.postRotate(rotation, bitmap.getWidth() * scale / 2, bitmap.getHeight() * scale / 2);
            matrix.postTranslate(x, y);
            float[] pts = { touchX, touchY };
            Matrix inverse = new Matrix();
            if (matrix.invert(inverse)) {
                inverse.mapPoints(pts);
                return pts[0] >= bitmap.getWidth() - handleSize && pts[0] <= bitmap.getWidth()
                        && pts[1] >= bitmap.getHeight() - handleSize && pts[1] <= bitmap.getHeight();
            }
            return false;
        }

        void draw(Canvas canvas, boolean isSelected) {
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            matrix.postRotate(rotation, bitmap.getWidth() * scale / 2, bitmap.getHeight() * scale / 2);
            matrix.postTranslate(x, y);
            canvas.drawBitmap(bitmap, matrix, null);

            if (isSelected) {
                float width = bitmap.getWidth() * scale;
                float height = bitmap.getHeight() * scale;

                Paint borderPaint = new Paint();
                borderPaint.setColor(Color.BLUE);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(4);
                borderPaint.setAntiAlias(true);

                canvas.drawRect(x, y, x + width, y + height, borderPaint);

                Paint handlePaint = new Paint();
                handlePaint.setColor(Color.BLUE);
                handlePaint.setStyle(Paint.Style.FILL);
                float handleSize = 20;
                canvas.save();
                canvas.translate(x + width - handleSize, y + height - handleSize);
                canvas.drawRect(0, 0, handleSize, handleSize, handlePaint);
                canvas.restore();
            }
        }
    }

    private void erasePath(float x, float y) {
        Iterator<Path> pathIterator = paths.iterator();
        Iterator<Integer> colorIterator = colors.iterator();

        while (pathIterator.hasNext() && colorIterator.hasNext()) {
            Path path = pathIterator.next();
            colorIterator.next();
            if (isPointNearPath(x, y, path)) {
                pathIterator.remove();
                colorIterator.remove();
                break;
            }
        }
    }

    private boolean isPointNearPath(float x, float y, Path path) {
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        return bounds.contains(x, y)
                || (Math.abs(bounds.centerX() - x) < eraseRadius && Math.abs(bounds.centerY() - y) < eraseRadius);
    }

    public void setColor(int color) {
        currentColor = color;
        isEraseMode = false;
        paint.setColor(currentColor);
        invalidate();
    }

    public void setEraseMode(boolean eraseMode) {
        isEraseMode = eraseMode;
        if (eraseMode) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(currentColor);
        }
        invalidate();
    }

    public void setStrokeWidth(float width) {
        paint.setStrokeWidth(width);
    }

    public void clear() {
        paths.clear();
        colors.clear();
        currentPath.reset();
        stickers.clear();
        invalidate();
    }

    public void drawRemoteLine(Line line) {
        if (line == null) return;

        Paint remotePaint = new Paint(paint);
        try {
            remotePaint.setColor(Color.parseColor(line.color));
        } catch (IllegalArgumentException | NullPointerException e) {
            remotePaint.setColor(Color.BLACK);
        }
        remotePaint.setStrokeWidth(line.width);

        Path remotePath = new Path();
        remotePath.moveTo(line.startX, line.startY);
        remotePath.lineTo(line.endX, line.endY);

        paths.add(remotePath);
        colors.add(remotePaint.getColor());

        invalidate();
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        backgroundBitmap = bitmap;
        invalidate();
    }

    public Bitmap exportBitmap() {
        Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        if (backgroundBitmap != null) canvas.drawBitmap(backgroundBitmap, 0, 0, backgroundPaint);

        for (Sticker sticker : stickers) {
            sticker.draw(canvas, false);
        }

        for (int i = 0; i < paths.size(); i++) {
            paint.setColor(colors.get(i));
            canvas.drawPath(paths.get(i), paint);
        }

        return result;
    }
}
