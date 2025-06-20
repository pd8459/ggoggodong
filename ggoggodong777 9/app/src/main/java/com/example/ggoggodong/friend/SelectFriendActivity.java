package com.example.ggoggodong.friend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ggoggodong.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private Button btnConfirm;
    private ArrayList<Friend> friendList = new ArrayList<>();
    private ArrayList<String> selectedFriendId = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        recyclerView = findViewById(R.id.recyclerViewFriends);
        btnConfirm = findViewById(R.id.btnConfirmSelection);
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new FriendAdapter(friendList, selectedFriendId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadFriends();

        btnConfirm.setOnClickListener(v -> {
            Log.d("선택된 친구", selectedFriendId.toString());
            if (selectedFriendId.isEmpty()) {
                Toast.makeText(this, "한 명 이상의 친구를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selectedFriends", selectedFriendId);
            resultIntent.putExtra("title", getIntent().getStringExtra("title"));
            resultIntent.putExtra("pageCount", getIntent().getIntExtra("pageCount", 0));
            setResult(RESULT_OK, resultIntent);
            finish();
        });


    }

    private void loadFriends() {
        String myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(myUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    friendList.clear();
                    List<String> friendUids = (List<String>) documentSnapshot.get("friends");

                    if (friendUids != null && !friendUids.isEmpty()) {
                        final int[] loadedFriendsCount = {0};
                        for (String friendUid : friendUids) {
                            db.collection("users")
                                    .document(friendUid)
                                    .get()
                                    .addOnSuccessListener(friendDoc -> {
                                        String username = friendDoc.getString("username");
                                        String email = friendDoc.getString("email");
                                        String profileImageUrl = friendDoc.getString("profileImageUrl");

                                        String displayName = (username != null && !username.isEmpty())
                                                ? username : (email != null ? email : "알 수 없음");

                                        friendList.add(new Friend(friendUid, displayName, profileImageUrl));


                                        loadedFriendsCount[0]++;
                                        if (loadedFriendsCount[0] == friendUids.size()) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SelectFriendActivity.this, "친구 정보 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SelectFriendActivity.this, "친구 목록 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
