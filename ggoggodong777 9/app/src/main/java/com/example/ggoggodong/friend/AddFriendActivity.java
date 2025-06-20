package com.example.ggoggodong.friend;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ggoggodong.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.WriteBatch;

public class AddFriendActivity extends AppCompatActivity {

    private EditText editTextFriendEmail;
    private Button buttonAddFriend;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend);

        editTextFriendEmail = findViewById(R.id.editTextFriendEmail);
        buttonAddFriend = findViewById(R.id.buttonAddFriend);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        buttonAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String friendEmail = editTextFriendEmail.getText().toString().trim();
                if (friendEmail.isEmpty()) {
                    Toast.makeText(AddFriendActivity.this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                addFriendByEmail(friendEmail);
            }
        });
    }

    private void addFriendByEmail(String friendEmail) {
        String myUid = auth.getCurrentUser().getUid();

        db.collection("users")
                .whereEqualTo("email", friendEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String friendUid = document.getString("uid");

                        if (myUid.equals(friendUid)) {
                            Toast.makeText(AddFriendActivity.this, "자기 자신을 추가할 수 없습니다", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        WriteBatch batch = db.batch();

                        batch.update(db.collection("users").document(myUid),
                                "friends", FieldValue.arrayUnion(friendUid));
                        batch.update(db.collection("users").document(friendUid),
                                "friends", FieldValue.arrayUnion(myUid));

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AddFriendActivity.this, "친구 추가 성공!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddFriendActivity.this, "친구 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("FriendAdd", "친구 추가 실패", e);
                                });

                    } else {
                        Toast.makeText(AddFriendActivity.this, "해당 이메일을 가진 사용자가 없습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddFriendActivity.this, "이메일 검색 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FriendAdd", "이메일 검색 실패", e);
                });
    }

}
