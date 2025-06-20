package com.example.ggoggodong;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ggoggodong.friend.AddFriendActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // 장르 버튼 설정
        findViewById(R.id.genre1).setOnClickListener(v -> openGenreActivity(1));
        findViewById(R.id.genre2).setOnClickListener(v -> openGenreActivity(2));
        findViewById(R.id.genre3).setOnClickListener(v -> openGenreActivity(3));
        findViewById(R.id.genre4).setOnClickListener(v -> openGenreActivity(4));
        findViewById(R.id.genre5).setOnClickListener(v -> openGenreActivity(5));
        findViewById(R.id.genre6).setOnClickListener(v -> openGenreActivity(6));
        findViewById(R.id.genre7).setOnClickListener(v -> openGenreActivity(7));

        // 프로필 이미지 설정
        profileImageView = findViewById(R.id.profileImage);
        loadProfileImage();

        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MyPageActivity.class);
            startActivity(intent);
        });

        // 친구 추가 버튼 클릭 시 add_friend.xml로 이동
        ImageButton btnAddFriend = findViewById(R.id.btnLeftTop);  // 버튼 ID 확인
        btnAddFriend.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddFriendActivity.class);  // AddFriendActivity로 이동
            startActivity(intent);
        });
    }

    private void openGenreActivity(int genre) {
        Intent intent = new Intent(HomeActivity.this, GenreActivity.class);
        intent.putExtra("genre", genre);
        startActivity(intent);
    }

    private void loadProfileImage() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("profileImageUrl")) {
                        String imageUrl = document.getString("profileImageUrl");
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.default_profile) // 기본 이미지
                                .into(profileImageView);
                    } else {
                        // 기본 이미지
                        profileImageView.setImageResource(R.drawable.default_profile);
                    }
                })
                .addOnFailureListener(e -> {
                    profileImageView.setImageResource(R.drawable.default_profile);
                });
    }
}
