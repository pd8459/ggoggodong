package com.example.ggoggodong.friend;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ggoggodong.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private FirebaseFirestore db;
    private List<Friend> friendList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_friends);

        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendsAdapter(friendList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadFriends();
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
                                        Toast.makeText(FriendsActivity.this, "친구 정보 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FriendsActivity.this, "친구 목록 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
