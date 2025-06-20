package com.example.ggoggodong.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ggoggodong.HomeActivity;
import com.example.ggoggodong.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;


public class ProfileActivity extends AppCompatActivity {

    private ProfileDrawingView profileDrawingView;
    private View[] containers;
    private Button[] buttons;
    private int activeContainer = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        profileDrawingView = findViewById(R.id.drawing_view);

        initializeColorButtons();
        initializeDrawerButtons();
        initializeStickerButtons();

        ImageButton finishButton = findViewById(R.id.finish);
        finishButton.setOnClickListener(v -> {
            saveProfileImage();
        });
    }

    private void initializeColorButtons() {
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
            ImageButton button = findViewById(pair[0]);
            int color = pair[1];

            button.setOnClickListener(v -> {
                profileDrawingView.setEraseMode(false); // 색 선택 시 지우개 모드 해제
                profileDrawingView.setColor(color);
            });
        }

        // 지우개 버튼
        ImageButton eraser = findViewById(R.id.color_eraser);
        eraser.setOnClickListener(v -> profileDrawingView.setEraseMode(true));
    }

    private void initializeDrawerButtons() {
        buttons = new Button[]{
                findViewById(R.id.sticker_button),
                findViewById(R.id.face_button),
                findViewById(R.id.eyes_button),
                findViewById(R.id.nose_button),
                findViewById(R.id.mouth_button),
                findViewById(R.id.hair_button)
        };

        containers = new View[]{
                findViewById(R.id.sticker_container),
                findViewById(R.id.face_container),
                findViewById(R.id.eyes_container),
                findViewById(R.id.nose_container),
                findViewById(R.id.mouth_container),
                findViewById(R.id.hair_container)
        };

        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].setOnClickListener(v -> toggleContainer(index));
        }
    }

    private void toggleContainer(int index) {
        if (activeContainer == index) {
            containers[index].setVisibility(View.GONE);
            activeContainer = -1;
        } else {
            for (View container : containers) {
                container.setVisibility(View.GONE);
            }
            containers[index].setVisibility(View.VISIBLE);
            activeContainer = index;
        }
    }

    private void initializeStickerButtons() {
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


        for (int i = 0; i < stickerIds.length; i++) {
            int viewId = stickerIds[i];
            int drawableId = drawableIds[i];

            ImageButton stickerBtn = findViewById(viewId);
            stickerBtn.setOnClickListener(v -> {
                Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), drawableId);

                // 👉 DrawingView의 반지름보다 작게 맞추기 위한 최대 스티커 크기
                int maxStickerWidth = 200;
                int maxStickerHeight = 200;

                int originalWidth = originalBitmap.getWidth();
                int originalHeight = originalBitmap.getHeight();

                // 👉 축소 비율 계산
                float scaleFactor = Math.min((float) maxStickerWidth / originalWidth,
                        (float) maxStickerHeight / originalHeight);
                if (scaleFactor > 1.0f) scaleFactor = 1.0f; // 너무 작지 않으면 원본 유지

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        originalBitmap,
                        (int) (originalWidth * scaleFactor),
                        (int) (originalHeight * scaleFactor),
                        true
                );

                // 👉 DrawingView 중심에 배치
                float centerX = profileDrawingView.getWidth() / 2f - scaledBitmap.getWidth() / 2f;
                float centerY = profileDrawingView.getHeight() / 2f - scaledBitmap.getHeight() / 2f;

                profileDrawingView.addSticker(scaledBitmap, centerX, centerY);
            });
        }
    }
    private void saveProfileImage() {
        Bitmap bitmap = getBitmapFromView(profileDrawingView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + uid + ".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();

        UploadTask uploadTask = profileImageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot ->
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(uid)
                                    .update("profileImageUrl", uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "프로필 이미지 저장 완료!", Toast.LENGTH_SHORT).show();

                                        // 👉 저장 성공 후 HomeActivity로 이동
                                        Intent intent = new Intent(this, HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "URL 저장 실패", Toast.LENGTH_SHORT).show());
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show());
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}