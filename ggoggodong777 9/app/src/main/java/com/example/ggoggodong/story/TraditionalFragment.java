package com.example.ggoggodong.story;

import static android.content.Intent.getIntent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ggoggodong.Book;
import com.example.ggoggodong.BookAdapter;
import com.example.ggoggodong.R;
import com.example.ggoggodong.friend.Friend;
import com.example.ggoggodong.friend.FriendsAdapter;
import com.example.ggoggodong.friend.SelectFriendActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;



public class TraditionalFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> storyEditLauncher;
    private FriendsAdapter friendsAdapter;


    private void handleTogetherStoryClick(Book book) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String docId = book.getDocId();
        db.collection("togetherStoriesTr").document(docId).get().addOnSuccessListener(doc -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, String> assignedUsers = (Map<String, String>) doc.get("assignedUsers");

            int myPageIndex = -1;
            for (String key : assignedUsers.keySet()) {
                if (uid.equals(assignedUsers.get(key))) {
                    myPageIndex = Integer.parseInt(key.replace("page_", ""));
                    break;
                }
            }

            // 참여 안한 경우 빈 슬롯 찾아 자동 배정
            if (myPageIndex == -1) {
                for (String key : assignedUsers.keySet()) {
                    if (assignedUsers.get(key) == null || assignedUsers.get(key).isEmpty()) {
                        myPageIndex = Integer.parseInt(key.replace("page_", ""));
                        db.collection("togetherStoriesTr").document(docId)
                                .update("assignedUsers." + key, uid);
                        break;
                    }
                }
            }

            if (myPageIndex != -1) {
                Intent intent = new Intent(getContext(), TraditionalStoryEditStoryActivity.class);
                intent.putExtra("title", doc.getString("title"));
                intent.putExtra("docId", docId);
                intent.putExtra("pageCount", doc.getLong("pageCount").intValue());
                intent.putExtra("participantCount", doc.getLong("participantCount").intValue());
                intent.putExtra("isTogether", true);
                intent.putExtra("myIndex", myPageIndex);
                storyEditLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "참여 가능한 페이지가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFriendStoryClick(Book book) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String docId = book.getDocId();
        Log.d("Firestore", "불러올 docId = " + docId);

        db.collection("friendTogetherStoriesTr").document(docId).get().addOnSuccessListener(doc -> {
            Log.d("Firestore", "문서 존재 여부: " + doc.exists());

            if (!doc.exists()) {
                Toast.makeText(getContext(), "저장된 동화를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            List<String> ownerIds = (List<String>) doc.get("ownerId");

            if (ownerIds != null && ownerIds.contains(uid)) {
                Long pageCountLong = doc.getLong("pageCount");
                int pageCount = (pageCountLong != null) ? pageCountLong.intValue() : 0;

                Long participantCountLong = doc.getLong("participantCount");
                int participantCount = (participantCountLong != null) ? participantCountLong.intValue() : 0;

                Intent intent = new Intent(getContext(), TraditionalStoryEditStoryActivity.class);
                intent.putExtra("title", doc.getString("title"));
                intent.putExtra("docId", docId);
                intent.putExtra("pageCount", pageCount);
                intent.putExtra("participantCount", participantCount);
                intent.putExtra("isWithFriend", true);
                storyEditLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "참여 중인 동화가 아닙니다.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "동화를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "문서 불러오기 실패", e);
        });
    }




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storyEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        ArrayList<String> selected = data.getStringArrayListExtra("selectedFriends");

                        if (selected == null) selected = new ArrayList<>();
                        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (!selected.contains(myUid)) {
                            selected.add(0, myUid);
                        }

                        Intent edit = new Intent(getContext(), TraditionalStoryEditStoryActivity.class);
                        edit.putExtra("title",      data.getStringExtra("title"));
                        edit.putExtra("pageCount",  data.getIntExtra("pageCount", 0));
                        edit.putStringArrayListExtra("selectedFriendUids", selected);
                        edit.putExtra("isWithFriend", true);

                        startActivity(edit);
                    }
                });

    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_traditional, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_tr);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        int spacing = (int) (getResources().getDisplayMetrics().density * 24);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(4, spacing, true));

        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList, getContext(), book -> {
            if (book.isCreateButton()) {
                showNewStoryPopup();

            } else if (book.isFriend()) {
                handleFriendStoryClick(book);

            } else if (book.isTogether()) {
                handleTogetherStoryClick(book);

            } else {
                Intent intent = new Intent(getContext(),
                        TraditionalStoryEditStoryActivity.class);
                intent.putExtra("docId", book.getDocId());
                storyEditLauncher.launch(intent);
            }
        });


        recyclerView.setAdapter(bookAdapter);
        db = FirebaseFirestore.getInstance();

        loadStories();
        return view;
    }

    private void showNewStoryPopup() {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_story, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(popupView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        Button btnAlone = popupView.findViewById(R.id.alone);
        Button btnFriends = popupView.findViewById(R.id.friends);
        Button btnTogether = popupView.findViewById(R.id.together);

        btnAlone.setOnClickListener(v -> {
            dialog.dismiss();
            // 혼자 스토리를 만들 때 다이얼로그 호출
            showAloneStoryPopup();
        });

        btnFriends.setOnClickListener(v -> {
            dialog.dismiss();
            // 친구와 함께 스토리를 만들 때 다이얼로그 호출
            showFriendsStoryPopup();
        });

        btnTogether.setOnClickListener(v -> {
            dialog.dismiss();
            // 함께 만들기 다이얼로그 호출
            showTogetherCreateDialog();
        });

        dialog.show();
    }

    private void showAloneStoryPopup() {
        // 혼자 스토리를 만들 때 필요한 다이얼로그
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_alone_story, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(popupView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        EditText titleInput = popupView.findViewById(R.id.editTitle);
        EditText pageInput = popupView.findViewById(R.id.editPageCount);
        Button btnCreate = popupView.findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String pageText = pageInput.getText().toString().trim();

            if (title.isEmpty() || pageText.isEmpty()) {
                Toast.makeText(getContext(), "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int pages = Integer.parseInt(pageText);

                if (pages <= 0) {
                    Toast.makeText(getContext(), "페이지 수는 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String docId = UUID.randomUUID().toString();

                // 스토리 생성 로직
                Intent intent = new Intent(getContext(), TraditionalStoryEditStoryActivity.class);
                intent.putExtra("docId", docId);
                intent.putExtra("title", title);
                intent.putExtra("pageCount", pages);
                intent.putExtra("isAlone", true);
                storyEditLauncher.launch(intent);
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "숫자를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showFriendsStoryPopup() {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_friends_story, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(popupView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        EditText titleInput = popupView.findViewById(R.id.editTitle);
        EditText pageInput = popupView.findViewById(R.id.editPageCount);
        Button btnCreate = popupView.findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String pageText = pageInput.getText().toString().trim();

            if (title.isEmpty() || pageText.isEmpty()) {
                Toast.makeText(getContext(), "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int pages = Integer.parseInt(pageText);
                if (pages <= 0) {
                    Toast.makeText(getContext(), "페이지 수는 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getContext(), SelectFriendActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("pageCount", pages);

                storyEditLauncher.launch(intent);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "숫자를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }







    private void showTogetherCreateDialog() {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_together_story, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(popupView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        EditText titleInput = popupView.findViewById(R.id.editTitle);
        Spinner spinnerMembers = popupView.findViewById(R.id.spinnerMembers);
        Spinner spinnerPages = popupView.findViewById(R.id.spinnerPages);
        Button btnCreate = popupView.findViewById(R.id.btnCreate);

        List<String> memberOptions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) memberOptions.add(String.valueOf(i));
        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, memberOptions);
        spinnerMembers.setAdapter(memberAdapter);

        spinnerMembers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int members = Integer.parseInt(memberOptions.get(position));
                List<String> pageOptions = new ArrayList<>();
                for (int i = 1; i <= 10; i++) {
                    if (i % members == 0) pageOptions.add(String.valueOf(i));
                }
                ArrayAdapter<String> pageAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, pageOptions);
                spinnerPages.setAdapter(pageAdapter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnCreate.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            int members = Integer.parseInt(spinnerMembers.getSelectedItem().toString());
            int pages = Integer.parseInt(spinnerPages.getSelectedItem().toString());

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String docId = UUID.randomUUID().toString();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("participantCount", members);
            data.put("pageCount", pages);
            data.put("ownerId", FirebaseAuth.getInstance().getCurrentUser().getUid());

            Map<String, String> assignedUsers = new HashMap<>();
            for (int i = 0; i < pages; i++) {
                if (i == 0) {
                    assignedUsers.put("page_" + i, uid); // 🔸 첫 번째 페이지를 생성자에게 할당
                } else {
                    assignedUsers.put("page_" + i, null);
                }
            }
            data.put("assignedUsers", assignedUsers);

            Map<String, Boolean> completedPages = new HashMap<>();
            for (int i = 0; i < pages; i++) completedPages.put("page_" + i, false);
            data.put("completedPages", completedPages);

            List<String> pageUrls = new ArrayList<>();
            for (int i = 0; i < pages; i++) pageUrls.add("");
            data.put("pageUrls", pageUrls);

            FirebaseFirestore.getInstance().collection("togetherStoriesTr")
                    .document(docId)
                    .set(data)
                    .addOnSuccessListener(unused -> {
                        Intent intent = new Intent(getContext(), TraditionalStoryEditStoryActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("docId", docId);
                        intent.putExtra("pageCount", pages);
                        intent.putExtra("participantCount", members);
                        intent.putExtra("isTogether", true);
                        intent.putExtra("myIndex", 0); // 시작자는 0번 인덱스
                        storyEditLauncher.launch(intent);
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "저장 실패", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }




    private void loadStories() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("traditionalstory")
                .whereArrayContains("ownerId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> aloneBooks = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Book book = doc.toObject(Book.class);
                        book.setCreateButton(false);
                        book.setDocId(doc.getId());
                        book.setTogether(false);
                        aloneBooks.add(book);
                    }

                    loadTogetherStories(aloneBooks);
                });

    }



    private void loadTogetherStories(List<Book> aloneBooks) {
        FirebaseFirestore.getInstance().collection("togetherStoriesTr")
                .get()
                .addOnSuccessListener(query -> {
                    List<Book> togetherBooks = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Book book = new Book();
                        book.setDocId(doc.getId());
                        book.setTitle(doc.getString("title"));
                        book.setCreateButton(false);
                        book.setTogether(true);
                        togetherBooks.add(book);
                    }

                    loadFriendTogetherStories(aloneBooks, togetherBooks);
                });
    }


    private void loadFriendTogetherStories(List<Book> aloneBooks, List<Book> togetherBooks) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("friendTogetherStoriesTr")
                .whereArrayContains("ownerId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    List<Book> friendBooks = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Book book = new Book();
                        book.setDocId(doc.getId());
                        book.setTitle(doc.getString("title"));
                        book.setCreateButton(false);
                        book.setTogether(true);
                        book.setFriend(true);
                        book.setTogether(false);

                        friendBooks.add(book);
                    }

                    bookList.clear();

                    Book createButton = new Book();
                    createButton.setCreateButton(true);
                    bookList.add(createButton);

                    bookList.addAll(aloneBooks);
                    bookList.addAll(togetherBooks);
                    bookList.addAll(friendBooks);

                    bookAdapter.notifyDataSetChanged();
                });
    }







    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount, spacing;
        private final boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) outRect.top = spacing;
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) outRect.top = spacing;
            }
        }
    }
}