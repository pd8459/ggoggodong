package com.example.ggoggodong;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.example.ggoggodong.story.TraditionalStoryEditStoryActivity;
import com.google.android.gms.tasks.Tasks;


import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ggoggodong.friend.Friend;
import com.example.ggoggodong.friend.FriendsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MyPageActivity extends AppCompatActivity {

    private ConstraintLayout greenArea;
    private ImageView profileImageView;
    private TextView userIdTextView, userGradeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        greenArea = findViewById(R.id.green_section);
        profileImageView = findViewById(R.id.profileImage);
        userIdTextView = findViewById(R.id.userId);
        userGradeTextView = findViewById(R.id.userGrade);

        loadProfileImage();
        loadUserInfo();
        loadAndApplyUserGrade();

        findViewById(R.id.btnGrade).setOnClickListener(v -> showPopup(R.layout.popup_grade));
        findViewById(R.id.btnFriends).setOnClickListener(v -> showPopup(R.layout.popup_friends));
        findViewById(R.id.btnChange).setOnClickListener(v -> showPopup(R.layout.popup_password));
        findViewById(R.id.btnMy).setOnClickListener(v -> showPopupMyStories());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
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
                                .placeholder(R.drawable.default_profile)
                                .into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.default_profile);
                    }
                })
                .addOnFailureListener(e -> profileImageView.setImageResource(R.drawable.default_profile));
    }

    private void loadUserInfo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String email = document.getString("email");
                        String grade = document.getString("grade");
                        userIdTextView.setText("ID: " + email);
                        userGradeTextView.setText("등급: " + (grade != null ? grade : "미정"));
                    }
                });
    }

    private void loadAndApplyUserGrade() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String[] togetherCollections = {
                "togetherStoriesTr", "togetherStoriesMr", "togetherStoriesFt",
                "togetherStoriesFm", "togetherStoriesFb", "togetherStoriesCm", "togetherStoriesAd"
        };

        String[] friendTogetherCollections = {
                "friendTogetherStoriesTr", "friendTogetherStoriesMr", "friendTogetherStoriesFt",
                "friendTogetherStoriesFm", "friendTogetherStoriesFb", "friendTogetherStoriesCm", "friendTogetherStoriesAd"
        };

        final int[] count = {0};

        // 혼자 만든 동화 수 세기
        List<com.google.android.gms.tasks.Task<?>> aloneTasks = new ArrayList<>();
        String[] aloneCollections = {
                "traditionalstory", "moralstory", "fantasystory",
                "familystory", "fablestory", "comicstory", "adventurestory"
        };
        for (String col : aloneCollections) {
            aloneTasks.add(
                    db.collection(col).get().addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query) {
                            Object owner = doc.get("ownerId");
                            if (owner instanceof String && uid.equals(owner)) {
                                count[0]++;
                            } else if (owner instanceof List) {
                                List<?> ownerList = (List<?>) owner;
                                if (ownerList.contains(uid)) {
                                    count[0]++;
                                }
                            }
                        }
                    })
            );
        }


        // 함께 만든 동화 수 세기
        List<com.google.android.gms.tasks.Task<?>> togetherTasks = new ArrayList<>();
        for (String col : togetherCollections) {
            togetherTasks.add(db.collection(col).get().addOnSuccessListener(query -> {
                for (DocumentSnapshot doc : query) {
                    Map<String, String> assignedUsers =(Map<String, String>) doc.get("assignedUsers");
                    if (assignedUsers != null && assignedUsers.containsValue(uid)) {
                        count[0]++;
                    }
                }
            }));
        }

        // 친구와 함께 만든 동화 수 세기 (수정된 코드)
        List<com.google.android.gms.tasks.Task<?>> friendtogetherTasks = new ArrayList<>();
        for (String col : friendTogetherCollections) {
            friendtogetherTasks.add(
                    db.collection(col).get().addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query) {
                            Object ownerObj = doc.get("ownerId");
                            if (ownerObj instanceof List) {
                                List<?> ownerList = (List<?>) ownerObj;
                                if (ownerList.contains(uid)) {
                                    count[0]++;
                                }
                            }
                        }
                    })
            );
        }




        // 모든 작업 완료 후 등급 계산 적용
        com.google.android.gms.tasks.Tasks.whenAllComplete(aloneTasks)
                .addOnSuccessListener(tasks -> com.google.android.gms.tasks.Tasks.whenAllComplete(togetherTasks)
                        .addOnSuccessListener(innerTasks -> Tasks.whenAllComplete(friendtogetherTasks)
                                .addOnSuccessListener(innerTasks2 -> {
                                    String gradeText = "등급: 미정";
                                    String gradeEmoji = "";

                                    if (count[0] >= 9) {
                                        gradeText = "등급: 책";
                                        gradeEmoji = "📘 ";
                                    } else if (count[0] >= 6) {
                                        gradeText = "등급: 종이";
                                        gradeEmoji = "📄 ";
                                    } else if (count[0] >= 3) {
                                        gradeText = "등급: 나무";
                                        gradeEmoji = "🌳 ";
                                    } else if (count[0] >= 1) {
                                        gradeText = "등급: 새싹";
                                        gradeEmoji = "🌱 ";
                                    }

                                    userGradeTextView.setText(gradeEmoji + gradeText);

                                    db.collection("users").document(uid)
                                    .update("grade", gradeText.replace("등급: ", ""));

                                })));
    }


    private void showPopup(int layoutResId) {
        View popupView = LayoutInflater.from(this).inflate(layoutResId, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                650,
                true
        );


        if (layoutResId == R.layout.popup_grade) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            int[] count = {0};

            String[] aloneCollections = {
                    "traditionalstory", "moralstory", "fantasystory",
                    "familystory", "fablestory", "comicstory", "adventurestory"
            };

            String[] togetherCollections = {
                    "togetherStoriesTr", "togetherStoriesMr", "togetherStoriesFt",
                    "togetherStoriesFm", "togetherStoriesFb", "togetherStoriesCm", "togetherStoriesAd"
            };

            String[] friendTogetherCollections = {
                    "friendTogetherStoriesTr", "friendTogetherStoriesMr", "friendTogetherStoriesFt",
                    "friendTogetherStoriesFm", "friendTogetherStoriesFb", "friendTogetherStoriesCm", "friendTogetherStoriesAd"
            };

            List<com.google.android.gms.tasks.Task<?>> aloneTasks = new ArrayList<>();
            List<com.google.android.gms.tasks.Task<?>> togetherTasks = new ArrayList<>();
            List<com.google.android.gms.tasks.Task<?>> friendtogetherTasks = new ArrayList<>();

            for (String col : aloneCollections) {
                aloneTasks.add(
                        db.collection(col).get().addOnSuccessListener(query -> {
                            for (DocumentSnapshot doc : query) {
                                Object ownerObj = doc.get("ownerId");
                                if (ownerObj instanceof String && uid.equals(ownerObj)) {
                                    count[0]++;
                                } else if (ownerObj instanceof List && ((List<?>) ownerObj).contains(uid)) {
                                    count[0]++;
                                }
                            }
                        })
                );
            }


            for (String col : friendTogetherCollections) {
                friendtogetherTasks.add(
                        db.collection(col).get().addOnSuccessListener(query -> {
                            for (DocumentSnapshot doc : query) {
                                Object ownerObj = doc.get("ownerId");
                                if (ownerObj instanceof List) {
                                    List<?> ownerList = (List<?>) ownerObj;
                                    if (ownerList.contains(uid)) {
                                        count[0]++;
                                    }
                                }
                            }
                        })
                );
            }


            for (String col : togetherCollections) {
                togetherTasks.add(
                        db.collection(col).get().addOnSuccessListener(query -> {
                            for (DocumentSnapshot doc : query) {
                                Map<String, String> assignedUsers = (Map<String, String>) doc.get("assignedUsers");
                                if (assignedUsers != null && assignedUsers.containsValue(uid)) {
                                    count[0]++;
                                }
                            }
                        })
                );
            }




            com.google.android.gms.tasks.Tasks.whenAllComplete(aloneTasks)
                    .addOnSuccessListener(t1 -> com.google.android.gms.tasks.Tasks.whenAllComplete(togetherTasks)
                            .addOnSuccessListener(t2 -> com.google.android.gms.tasks.Tasks.whenAllComplete(friendtogetherTasks)
                                    .addOnSuccessListener(t3 -> applyGradeStyle(popupView, count[0]))));

        }




        // ✅ 비밀번호 변경 기능 연결
        if (layoutResId == R.layout.popup_password) {
            EditText newPw = popupView.findViewById(R.id.editNewPassword);
            EditText confirmPw = popupView.findViewById(R.id.editConfirmPassword);
            Button changeBtn = popupView.findViewById(R.id.btnChangePassword);

            changeBtn.setOnClickListener(v -> {
                String pw1 = newPw.getText().toString();
                String pw2 = confirmPw.getText().toString();

                if (pw1.isEmpty() || pw2.isEmpty()) {
                    Toast.makeText(this, "비밀번호를 모두 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pw1.equals(pw2)) {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance().getCurrentUser()
                        .updatePassword(pw1)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show();
                            popupWindow.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "변경 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            });
        }


        if (layoutResId == R.layout.popup_friends) {
            RecyclerView recyclerViewFriends = popupView.findViewById(R.id.recyclerViewFriends);
            TextView titleTextView = popupView.findViewById(R.id.textViewTitle);

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && document.contains("friends")) {
                            List<String> friends = (List<String>) document.get("friends");
                            if (friends != null && !friends.isEmpty()) {

                                loadFriendDetails(friends, recyclerViewFriends, titleTextView);
                            } else {
                                titleTextView.setText("친구 목록이 없습니다.");
                            }
                        } else {
                            titleTextView.setText("친구 목록을 불러오는 데 실패했습니다.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        titleTextView.setText("친구 목록을 불러오는 데 실패했습니다.");
                    });
        }

        if (greenArea != null) {
            greenArea.post(() -> {
                int[] location = new int[2];
                greenArea.getLocationOnScreen(location);
                int greenCenterY = location[1] + greenArea.getHeight() / 2;
                popupWindow.showAtLocation(greenArea, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, greenCenterY - 300);
            });
        }
    }

    private void applyGradeStyle(View popupView, int storyCount) {
        TextView gradeText = popupView.findViewById(R.id.gradeText);
        View sesak = popupView.findViewById(R.id.grade_sesak);
        View tree = popupView.findViewById(R.id.grade_tree);
        View paper = popupView.findViewById(R.id.grade_paper);
        View book = popupView.findViewById(R.id.grade_book);

        sesak.setBackground(null);
        tree.setBackground(null);
        paper.setBackground(null);
        book.setBackground(null);

        if (storyCount >= 9) {
            book.setBackgroundResource(R.drawable.bg_grade_highlight);
            gradeText.setText("📘 등급: 책");
        } else if (storyCount >= 6) {
            paper.setBackgroundResource(R.drawable.bg_grade_highlight);
            gradeText.setText("📄 등급: 종이");
        } else if (storyCount >= 3) {
            tree.setBackgroundResource(R.drawable.bg_grade_highlight);
            gradeText.setText("🌳 등급: 나무");
        } else if (storyCount >= 1) {
            sesak.setBackgroundResource(R.drawable.bg_grade_highlight);
            gradeText.setText("🌱 등급: 새싹");
        } else {
            gradeText.setText("등급 없음");
        }
    }

    private void loadFriendDetails(List<String> friends, RecyclerView recyclerView, TextView titleTextView) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        List<Friend> friendList = new ArrayList<>();

        for (String friendUid : friends) {
            firestore.collection("users")
                    .document(friendUid)
                    .get()
                    .addOnSuccessListener(friendDoc -> {
                        if (friendDoc.exists()) {
                            String username = friendDoc.getString("username");
                            String email = friendDoc.getString("email");
                            String profileImageUrl = friendDoc.getString("profileImageUrl");

                            String displayName = (username != null && !username.isEmpty())
                                    ? username : (email != null ? email : "알 수 없음");

                            friendList.add(new Friend(friendUid, displayName, profileImageUrl));

                            if (friendList.size() == friends.size()) {
                                FriendsAdapter adapter = new FriendsAdapter(friendList);
                                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "친구 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
        }
    }




    private void showPopupMyStories() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_my, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                1200,
                true
        );

        LinearLayout aloneSection = popupView.findViewById(R.id.aloneStorySection);
        LinearLayout togetherSection = popupView.findViewById(R.id.togetherStorySection);
        LinearLayout friendTogetherSection = popupView.findViewById(R.id.friendTogetherStorySection);


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String[] aloneCollections = {
                "traditionalstory", "moralstory", "fantasystory",
                "familystory", "fablestory", "comicstory", "adventurestory"
        };

        String[] togetherCollections = {
                "togetherStoriesTr", "togetherStoriesMr", "togetherStoriesFt",
                "togetherStoriesFm", "togetherStoriesFb", "togetherStoriesCm", "togetherStoriesAd"
        };

        String[] friendTogetherCollections = {
                "friendTogetherStoriesTr", "friendTogetherStoriesMr", "friendTogetherStoriesFt",
                "friendTogetherStoriesFm", "friendTogetherStoriesFb", "friendTogetherStoriesCm", "friendTogetherStoriesAd"
        };

        for (String col : aloneCollections) {
            db.collection(col)
                    .get()
                    .addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query) {
                            Object ownerObj = doc.get("ownerId");
                            if (ownerObj instanceof String && uid.equals(ownerObj)) {
                                addStoryToLayout(this, doc, aloneSection, popupWindow);
                            } else if (ownerObj instanceof List && ((List<?>) ownerObj).contains(uid)) {
                                addStoryToLayout(this, doc, aloneSection, popupWindow);
                            }
                        }
                    });
        }

        for (String col : togetherCollections) {
            db.collection(col)
                    .get()
                    .addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query) {
                            Map<String, String> assignedUsers = (Map<String, String>) doc.get("assignedUsers");
                            if (assignedUsers != null && assignedUsers.containsValue(uid)) {
                                addStoryToLayout(this, doc, togetherSection, popupWindow);
                            }
                        }
                    });
        }

        for (String col : friendTogetherCollections) {
            db.collection(col)
                    .get()
                    .addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query) {
                            Object ownerObj = doc.get("ownerId");
                            if (ownerObj instanceof List) {
                                List<?> ownerList = (List<?>) ownerObj;
                                if (ownerList.contains(uid)) {
                                    addStoryToLayout(this, doc, friendTogetherSection, popupWindow);
                                }
                            }
                        }
                    });
        }





        if (greenArea != null) {
            greenArea.post(() -> {
                int[] location = new int[2];
                greenArea.getLocationOnScreen(location);
                int greenCenterY = location[1] + greenArea.getHeight() / 2;
                popupWindow.showAtLocation(greenArea, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, greenCenterY - 300);
            });
        }
    }


    private void addStoryToLayout(
            MyPageActivity context,
            DocumentSnapshot doc,
            LinearLayout targetLayout,
            PopupWindow popupWindow
    ) {
        String title = doc.getString("title");

        View itemView = LayoutInflater.from(context).inflate(R.layout.item_story_thumbnail, targetLayout, false);
        ImageView bookImage = itemView.findViewById(R.id.bookImage);
        TextView bookTitle = itemView.findViewById(R.id.bookTitle);

        bookImage.setImageResource(R.drawable.ic_menu_book);
        bookTitle.setText(title);

        String path = doc.getReference().getPath();
        boolean isFriendTogether = path.contains("friendTogether");
        boolean isTogether = !isFriendTogether && path.contains("together");



        String collectionName = path.split("/")[0];


        Class<?> activityClass;
        switch (collectionName) {
            case "traditionalstory":
            case "togetherStoriesTr":
            case "friendTogetherStoriesTr":
                activityClass = com.example.ggoggodong.story.TraditionalStoryEditStoryActivity.class;
                break;
            case "moralstory":
            case "togetherStoriesMr":
            case "friendTogetherStoriesMr":
                activityClass = com.example.ggoggodong.story.MoralEditStoryActivity.class;
                break;
            case "fantasystory":
            case "togetherStoriesFt":
            case "friendTogetherStoriesFt":
                activityClass = com.example.ggoggodong.story.FantasyEditStoryActivity.class;
                break;
            case "familystory":
            case "togetherStoriesFm":
            case "friendTogetherStoriesFm":
                activityClass = com.example.ggoggodong.story.FamilyEditStoryActivity.class;
                break;
            case "fablestory":
            case "togetherStoriesFb":
            case "friendTogetherStoriesFb":
                activityClass = com.example.ggoggodong.story.FableEditStoryActivity.class;
                break;
            case "comicstory":
            case "togetherStoriesCm":
            case "friendTogetherStoriesCm":
                activityClass = com.example.ggoggodong.story.ComicEditStoryActivity.class;
                break;
            case "adventurestory":
            case "togetherStoriesAd":
            case "friendTogetherStoriesAd":
                activityClass = com.example.ggoggodong.story.AdventureEditStoryActivity.class;
                break;
            default:
                Toast.makeText(context, "알 수 없는 동화 유형입니다.", Toast.LENGTH_SHORT).show();
                return;
        }


        if (isFriendTogether) {
            itemView.setBackgroundResource(R.drawable.bg_book_card_blue); // 친구와 함께 → 하늘색
        } else if (isTogether) {
            itemView.setBackgroundResource(R.drawable.bg_book_card_green); // 모두와 함께 → 연두색
        } else {
            itemView.setBackgroundResource(R.drawable.bg_book_card); // 혼자하기 → 노란색
        }


        itemView.setOnClickListener(v -> {
            popupWindow.dismiss();

            Intent intent = new Intent(context, activityClass);
            intent.putExtra("docId", doc.getId());
            intent.putExtra("title", title);
            intent.putExtra("isTogether", isTogether);
            intent.putExtra("isWithFriend", isFriendTogether);
            intent.putExtra("collectionName", collectionName);


            Object ownerIdObj = doc.get("ownerId");
            if (ownerIdObj instanceof List) {
                List<String> ownerIds = (List<String>) ownerIdObj;
                intent.putStringArrayListExtra("ownerIds", new ArrayList<>(ownerIds));
            }

            Long pageCount = doc.getLong("pageCount");
            if (pageCount != null) {
                intent.putExtra("pageCount", pageCount.intValue());
            }

            Long participantCount = doc.getLong("participantCount");
            if (participantCount != null) {
                intent.putExtra("participantCount", participantCount.intValue());
            }

            context.startActivity(intent);
        });

        targetLayout.addView(itemView);
    }




}
