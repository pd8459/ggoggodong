package com.example.ggoggodong.story;

import android.view.ViewGroup;
import android.view.ViewParent;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.GestureDetector;

import android.view.Gravity;
import java.util.ArrayList;
import java.util.List;


import com.example.ggoggodong.R;

public class DrawingUIHelper {

    public static void setupColorButtons(Activity activity, StoryDrawingView drawingView) {
        int[][] buttonColorPairs = {
                {R.id.color_red, Color.RED},
                {R.id.color_orange, Color.parseColor("#FFA500")},
                {R.id.color_yellow, Color.YELLOW},
                {R.id.color_chartreuse, Color.parseColor("#7FFF00")},
                {R.id.color_green, Color.parseColor("#228B22")},
                {R.id.color_sky_blue, Color.parseColor("#87CEEB")},
                {R.id.color_blue, Color.BLUE},
                {R.id.color_purple, Color.parseColor("#800080")},
                {R.id.color_pink, Color.parseColor("#FFC0CB")},
                {R.id.color_black, Color.BLACK}
        };

        for (int[] pair : buttonColorPairs) {
            ImageButton button = activity.findViewById(pair[0]);
            int color = pair[1];
            button.setOnClickListener(v -> {
                drawingView.setEraseMode(false);
                drawingView.setColor(color);
            });
        }

        ImageButton eraser = activity.findViewById(R.id.color_eraser);
        eraser.setOnClickListener(v -> drawingView.setEraseMode(true));
    }


    public static void setupDrawerToggles(Activity activity, int btn1Id, int btn2Id,
                                          int topDrawerId, int scroll1Id, int scroll2Id) {

        ImageButton btn1 = activity.findViewById(btn1Id);
        ImageButton btn2 = activity.findViewById(btn2Id);
        View topDrawer = activity.findViewById(topDrawerId);
        View scroll1 = activity.findViewById(scroll1Id);
        View scroll2 = activity.findViewById(scroll2Id);

        final int[] currentVisible = {0};

        btn1.setOnClickListener(v -> {
            btn1.setEnabled(false);
            if (topDrawer.getVisibility() == View.GONE || currentVisible[0] != 1) {
                scroll1.setVisibility(View.VISIBLE);
                scroll2.setVisibility(View.GONE);
                topDrawer.setVisibility(View.VISIBLE);
                topDrawer.post(() -> {
                    topDrawer.setTranslationY(-topDrawer.getHeight());
                    topDrawer.animate()
                            .translationY(0)
                            .setDuration(300)
                            .withEndAction(() -> {
                                currentVisible[0] = 1;
                                btn1.setEnabled(true);
                            }).start();
                });
            } else {
                topDrawer.animate()
                        .translationY(-topDrawer.getHeight())
                        .setDuration(300)
                        .withEndAction(() -> {
                            topDrawer.setVisibility(View.GONE);
                            currentVisible[0] = 0;
                            btn1.setEnabled(true);
                        }).start();
            }
        });

        btn2.setOnClickListener(v -> {
            btn2.setEnabled(false);
            if (topDrawer.getVisibility() == View.GONE || currentVisible[0] != 2) {
                scroll1.setVisibility(View.GONE);
                scroll2.setVisibility(View.VISIBLE);
                topDrawer.setVisibility(View.VISIBLE);
                topDrawer.post(() -> {
                    topDrawer.setTranslationY(-topDrawer.getHeight());
                    topDrawer.animate()
                            .translationY(0)
                            .setDuration(300)
                            .withEndAction(() -> {
                                currentVisible[0] = 2;
                                btn2.setEnabled(true);
                            }).start();
                });
            } else {
                topDrawer.animate()
                        .translationY(-topDrawer.getHeight())
                        .setDuration(300)
                        .withEndAction(() -> {
                            topDrawer.setVisibility(View.GONE);
                            currentVisible[0] = 0;
                            btn2.setEnabled(true);
                        }).start();
            }
        });
    }

    public static void showTextInputDialog(Activity activity, FrameLayout container) {
        EditText input = new EditText(activity);
        input.setHint("글을 입력하세요");

        new AlertDialog.Builder(activity)
                .setTitle("글 추가")
                .setView(input)
                .setPositiveButton("확인", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        addDraggableTextView(activity, container, text);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    public static List<View> allHandles = new ArrayList<>();
    public static View currentVisibleHandle = null;

    public static void addDraggableTextView(Activity activity, FrameLayout container, String text) {
        FrameLayout wrapper = new FrameLayout(activity);

        TextView textView = new TextView(activity);
        textView.setText(text);
        textView.setTextSize(18);
        textView.setBackgroundColor(Color.argb(100, 255, 255, 255));
        textView.setPadding(20, 10, 20, 10);

        // 회전/크기조절 핸들 (파란 원)
        View handle = new View(activity);
        handle.setBackgroundColor(Color.BLUE);
        FrameLayout.LayoutParams handleParams = new FrameLayout.LayoutParams(30, 30);
        handleParams.gravity = Gravity.BOTTOM | Gravity.END;
        handle.setLayoutParams(handleParams);
        handle.setVisibility(View.GONE); // 기본은 숨김
        allHandles.add(handle);

        // wrapper 구성
        wrapper.addView(textView);
        wrapper.addView(handle);

        FrameLayout.LayoutParams wrapperParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapper.setLayoutParams(wrapperParams);
        wrapper.setX(100);
        wrapper.setY(100);
        wrapper.setScaleX(1f);
        wrapper.setScaleY(1f);
        wrapper.setRotation(0f);

        GestureDetector gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showTextEditDialog(activity, textView, wrapper);
                return true;
            }
        });

        View.OnTouchListener dragListener = new View.OnTouchListener() {
            float lastX = 0, lastY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    for (View h : allHandles) h.setVisibility(View.GONE);
                    handle.setVisibility(View.VISIBLE);
                    currentVisibleHandle = handle;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - lastX;
                        float dy = event.getRawY() - lastY;
                        wrapper.setX(wrapper.getX() + dx);
                        wrapper.setY(wrapper.getY() + dy);
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        return true;
                }
                return false;
            }
        };

        wrapper.setOnTouchListener(dragListener);
        textView.setOnTouchListener(dragListener);

        handle.setOnTouchListener(new View.OnTouchListener() {
            float startX, startY, startRotation, startScale;
            float centerX, centerY;
            float startAngle;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getRawX();
                float y = event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        centerX = wrapper.getX() + wrapper.getWidth() / 2f;
                        centerY = wrapper.getY() + wrapper.getHeight() / 2f;

                        startX = x;
                        startY = y;
                        startRotation = wrapper.getRotation();
                        startScale = wrapper.getScaleX();

                        float dx = x - centerX;
                        float dy = y - centerY;
                        startAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float dxMove = x - centerX;
                        float dyMove = y - centerY;
                        float angle = (float) Math.toDegrees(Math.atan2(dyMove, dxMove));
                        float angleDiff = angle - startAngle;

                        float distanceStart = (float) Math.hypot(startX - centerX, startY - centerY);
                        float distanceNow = (float) Math.hypot(x - centerX, y - centerY);
                        float scaleFactor = distanceNow / distanceStart;
                        scaleFactor = Math.max(0.3f, Math.min(3f, scaleFactor));

                        wrapper.setRotation(startRotation + angleDiff);
                        wrapper.setScaleX(startScale * scaleFactor);
                        wrapper.setScaleY(startScale * scaleFactor);
                        break;
                }
                return true;
            }
        });

        container.addView(wrapper);
    }



    private static void showTextEditDialog(Activity activity, TextView textView, View wrapper) {
        EditText input = new EditText(activity);
        input.setText(textView.getText().toString());

        new AlertDialog.Builder(activity)
                .setTitle("텍스트 수정")
                .setView(input)
                .setPositiveButton("확인", (dialog, which) -> {
                    String newText = input.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        textView.setText(newText);
                    }
                })
                .setNegativeButton("취소", null)
                .setNeutralButton("삭제", (dialog, which) -> {
                    ViewParent parent = wrapper.getParent();
                    if (parent instanceof ViewGroup) {
                        ((ViewGroup) parent).removeView(wrapper);
                    }
                })
                .show();
    }
    public static void hideAllHandles() {
        for (View h : allHandles) {
            h.setVisibility(View.GONE);
        }
        currentVisibleHandle = null;
    }

    public static void initializeDrawerButtons(Activity activity, int[] buttonIds, int[] containerIds) {
        final int[] activeContainer = {-1};

        for (int i = 0; i < buttonIds.length; i++) {
            final Button button = activity.findViewById(buttonIds[i]);
            final View container = activity.findViewById(containerIds[i]);
            final int index = i;

            button.setOnClickListener(v -> {
                if (activeContainer[0] == index) {
                    container.setVisibility(View.GONE);
                    activeContainer[0] = -1;
                } else {
                    for (int j = 0; j < containerIds.length; j++) {
                        activity.findViewById(containerIds[j]).setVisibility(View.GONE);
                    }
                    container.setVisibility(View.VISIBLE);
                    activeContainer[0] = index;
                }
            });
        }
    }

    // ✅ 스티커 버튼 등록
    public static void setupStickerButtons(Activity activity, int[] stickerIds, int[] drawableIds, StoryDrawingView drawingView) {
        Resources res = activity.getResources();
        for (int i = 0; i < stickerIds.length; i++) {
            int viewId = stickerIds[i];
            int drawableId = drawableIds[i];
            ImageButton stickerBtn = activity.findViewById(viewId);
            stickerBtn.setOnClickListener(v -> {
                Bitmap originalBitmap = BitmapFactory.decodeResource(res, drawableId);
                int maxWidth = 200, maxHeight = 200;
                float scale = Math.min((float) maxWidth / originalBitmap.getWidth(), (float) maxHeight / originalBitmap.getHeight());
                if (scale > 1.0f) scale = 1.0f;
                Bitmap scaled = Bitmap.createScaledBitmap(originalBitmap,
                        (int) (originalBitmap.getWidth() * scale),
                        (int) (originalBitmap.getHeight() * scale),
                        true);
                float cx = drawingView.getWidth() / 2f - scaled.getWidth() / 2f;
                float cy = drawingView.getHeight() / 2f - scaled.getHeight() / 2f;
                drawingView.addSticker(scaled, cx, cy);
            });
        }
    }

    // ✅ 스티커 초기화 (자동 호출용)
    public static void initializeStickerButtons(Activity activity, StoryDrawingView drawingView) {
        int[] stickerIds = {
                R.id.sticker1, R.id.sticker2, R.id.sticker3, R.id.sticker4, R.id.sticker5,
                R.id.sticker6, R.id.sticker7, R.id.sticker8, R.id.sticker9,
                R.id.sticker12, R.id.sticker13, R.id.sticker14, // 먼저 등장함
                R.id.sticker10, R.id.sticker11,
                R.id.sticker15, R.id.sticker16, R.id.sticker17, R.id.sticker18, R.id.sticker19,
                R.id.sticker20, R.id.sticker21, R.id.sticker22, R.id.sticker23, R.id.sticker24,
                R.id.sticker25, R.id.sticker26, R.id.sticker27,
                R.id.face1, R.id.face2, R.id.face3, R.id.face4,
                R.id.eyes1,R.id.eyes2,R.id.eyes3,R.id.eyes4,R.id.eyes5,
                R.id.eyes6,R.id.eyes7,R.id.eyes8,R.id.eyes9,R.id.eyes10,
                R.id.eyes11,R.id.eyes12,R.id.eyes13,R.id.eyes14,R.id.eyes15,
                R.id.eyes16,R.id.eyes17,R.id.eyes18,R.id.eyes19,R.id.eyes20,
                R.id.eyes21,R.id.eyes22,R.id.eyes23,R.id.eyes24,
                R.id.nose1,R.id.nose2,R.id.nose3,R.id.nose4,R.id.nose5,
                R.id.nose6,R.id.nose7,R.id.nose8,R.id.nose9,R.id.nose10,
                R.id.nose11,R.id.nose12,R.id.nose13,R.id.nose14,R.id.nose15,
                R.id.nose16,R.id.nose17,R.id.nose18,R.id.nose19,R.id.nose20,
                R.id.nose21,R.id.nose22,R.id.nose23,R.id.nose24,
                R.id.mouth1, R.id.mouth2, R.id.mouth3, R.id.mouth4, R.id.mouth5,
                R.id.mouth6, R.id.mouth7, R.id.mouth8, R.id.mouth9, R.id.mouth10,
                R.id.mouth11, R.id.mouth12, R.id.mouth13, R.id.mouth14, R.id.mouth15,
                R.id.mouth16, R.id.mouth17, R.id.mouth18, R.id.mouth19, R.id.mouth20,
                R.id.mouth21, R.id.mouth22, R.id.mouth23, R.id.mouth24, R.id.mouth25,
                R.id.hair1, R.id.hair2, R.id.hair3, R.id.hair4, R.id.hair5,
                R.id.hair6, R.id.hair7, R.id.hair8
        };

        int[] drawableIds = {
                R.drawable.sticker1, R.drawable.sticker2, R.drawable.sticker3, R.drawable.sticker4, R.drawable.sticker5,
                R.drawable.sticker6, R.drawable.sticker7, R.drawable.sticker8, R.drawable.sticker9,
                R.drawable.sticker12, R.drawable.sticker13, R.drawable.sticker14,
                R.drawable.sticker10, R.drawable.sticker11,
                R.drawable.sticker15, R.drawable.sticker16, R.drawable.sticker17, R.drawable.sticker18, R.drawable.sticker19,
                R.drawable.sticker20, R.drawable.sticker21, R.drawable.sticker22, R.drawable.sticker23, R.drawable.sticker24,
                R.drawable.sticker25, R.drawable.sticker26, R.drawable.sticker27,
                R.drawable.face1, R.drawable.face2, R.drawable.face3, R.drawable.face4,
                R.drawable.eyes1, R.drawable.eyes2, R.drawable.eyes3, R.drawable.eyes4, R.drawable.eyes5,
                R.drawable.eyes6, R.drawable.eyes7, R.drawable.eyes8, R.drawable.eyes9, R.drawable.eyes10,
                R.drawable.eyes11, R.drawable.eyes12, R.drawable.eyes13, R.drawable.eyes14, R.drawable.eyes15,
                R.drawable.eyes16, R.drawable.eyes17, R.drawable.eyes18, R.drawable.eyes19, R.drawable.eyes20,
                R.drawable.eyes21, R.drawable.eyes22, R.drawable.eyes23, R.drawable.eyes24,
                R.drawable.nose1, R.drawable.nose2, R.drawable.nose3, R.drawable.nose4, R.drawable.nose5,
                R.drawable.nose6, R.drawable.nose7, R.drawable.nose8, R.drawable.nose9, R.drawable.nose10,
                R.drawable.nose11, R.drawable.nose12, R.drawable.nose13, R.drawable.nose14, R.drawable.nose15,
                R.drawable.nose16, R.drawable.nose17, R.drawable.nose18, R.drawable.nose19, R.drawable.nose20,
                R.drawable.nose21, R.drawable.nose22, R.drawable.nose23, R.drawable.nose24,
                R.drawable.mouth1, R.drawable.mouth2, R.drawable.mouth3, R.drawable.mouth4, R.drawable.mouth5,
                R.drawable.mouth6, R.drawable.mouth7, R.drawable.mouth8, R.drawable.mouth9, R.drawable.mouth10,
                R.drawable.mouth11, R.drawable.mouth12, R.drawable.mouth13, R.drawable.mouth14, R.drawable.mouth15,
                R.drawable.mouth16, R.drawable.mouth17, R.drawable.mouth18, R.drawable.mouth19, R.drawable.mouth20,
                R.drawable.mouth21, R.drawable.mouth22, R.drawable.mouth23, R.drawable.mouth24, R.drawable.mouth25,
                R.drawable.hair1, R.drawable.hair2, R.drawable.hair3, R.drawable.hair4, R.drawable.hair5,
                R.drawable.hair6, R.drawable.hair7, R.drawable.hair8
        };
        setupStickerButtons(activity, stickerIds, drawableIds, drawingView);
    }
    public static void setupBackgroundButtons(Activity activity, StoryDrawingView drawingView) {
        int[] backgroundBtnIds = {
                R.id.background1, R.id.background2,R.id.background3,R.id.background4,R.id.background5,
                R.id.background6, R.id.background7,R.id.background8,R.id.background9,R.id.background10,
                R.id.background11,R.id.background12,R.id.background13,R.id.background14,R.id.background15,
                R.id.background16,R.id.background17,R.id.background18,R.id.background19,R.id.background20,
                R.id.background21,R.id.background22,R.id.background23,R.id.background24

                // 여기에 추가로 background3, 4... 필요 시 확장 가능
        };

        int[] backgroundDrawableIds = {
                R.drawable.background1, R.drawable.background2,R.drawable.background3,R.drawable.background4,R.drawable.background5,
                R.drawable.background6,R.drawable.background7,R.drawable.background8,R.drawable.background9,R.drawable.background10,
                R.drawable.background11,R.drawable.background12,R.drawable.background13,R.drawable.background14,R.drawable.background15,
                R.drawable.background16,R.drawable.background17,R.drawable.background18,R.drawable.background19,R.drawable.background20,
                R.drawable.background21,R.drawable.background22,R.drawable.background23,R.drawable.background24
        };

        for (int i = 0; i < backgroundBtnIds.length; i++) {
            int btnId = backgroundBtnIds[i];
            int drawableId = backgroundDrawableIds[i];

            ImageButton btn = activity.findViewById(btnId);
            btn.setOnClickListener(v -> {
                Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), drawableId);

                // 뷰 사이즈가 아직 0일 수 있으므로 ViewTreeObserver 사용
                drawingView.post(() -> {
                    int w = drawingView.getWidth();
                    int h = drawingView.getHeight();
                    if (w > 0 && h > 0) {
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, w, h, true);
                        drawingView.setBackgroundBitmap(scaled);
                    }
                });
            });
        }
    }

}