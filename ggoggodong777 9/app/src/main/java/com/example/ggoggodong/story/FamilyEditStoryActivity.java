package com.example.ggoggodong.story;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ggoggodong.R;
import com.example.ggoggodong.model.Line;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.widget.LinearLayout;
import com.example.ggoggodong.story.DrawingUIHelper;

public class FamilyEditStoryActivity extends AppCompatActivity implements StoryDrawingView.OnStrokeCompleteListener {

    private int participantCount;
    private int myIndex;
    private List<Integer> myPageIndices;
    private TextView pageIndicator;
    private TextView myPageInfo, progressInfo;
    private StoryDrawingView drawingView;
    private int currentPage = 0;
    private int totalPages;
    private List<StoryPage> pages;

    private String title;
    private String docId;
    private int coverResId;
    private String category;
    private FrameLayout drawingContainer;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private List<String> pageUrls;
    private Map<String, Boolean> completedPages;
    private Map<String, String> assignedUsers;
    private ArrayList<String> selectedFriendUids;

    private ImageButton btnPrev, btnNext, btnSave, btnCancel;


    private DatabaseReference linesRef;

    private void deleteAllPagesFromStorage(String title, int totalPages) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        for (int i = 0; i < totalPages; i++) {
            String fileName = "story_drawings/" + title + "/page_" + (i + 1) + ".png";
            StorageReference imageRef = storage.getReference().child(fileName);
            imageRef.delete()
                    .addOnSuccessListener(unused -> {
                        // 성공 로그 출력해도 되고 생략 가능
                    })
                    .addOnFailureListener(e -> {
                        // 삭제 실패 시에도 그냥 넘어감 (예: 이미지가 없을 수도 있음)
                    });
        }
    }

    private boolean isTogether = false;
    private boolean isWithFriend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isTogether = getIntent().getBooleanExtra("isTogether", false);
        isWithFriend = getIntent().getBooleanExtra("isWithFriend", false);
        selectedFriendUids = getIntent().getStringArrayListExtra("selectedFriendUids");
        if (selectedFriendUids == null) selectedFriendUids = new ArrayList<>();

        if (!isTogether) {
            // 혼자하기 또는 친구와 함께하기 모드일 때
            setContentView(R.layout.activity_story_page_edit2);
        } else {
            // 모두와 함께 모드일 때
            setContentView(R.layout.activity_story_page_edit);
        }
        ImageButton btnAddText = findViewById(R.id.btn_add_text);
        btnAddText.setOnClickListener(v -> {
            DrawingUIHelper.showTextInputDialog(FamilyEditStoryActivity.this, drawingContainer);
        });
        selectedFriendUids = getIntent().getStringArrayListExtra("selectedFriendUids");

        drawingView = findViewById(R.id.drawing_view);
        DrawingUIHelper.hideAllHandles();
        StoryDrawingView drawingView = findViewById(R.id.drawing_view);
        DrawingUIHelper.setupBackgroundButtons(this, drawingView);

        // ✅ 공통 색상 버튼 설정
        DrawingUIHelper.setupColorButtons(this, drawingView);

        // ✅ 공통 drawer 애니메이션 설정
        DrawingUIHelper.setupDrawerToggles(this,
                R.id.btn_toggle_colors,
                R.id.btn_toggle_colors2,
                R.id.top_drawer,
                R.id.color_scroll,
                R.id.color_scroll_2);

        int[] buttonIds = {
                R.id.sticker_button,
                R.id.face_button,
                R.id.eyes_button,
                R.id.nose_button,
                R.id.mouth_button,
                R.id.hair_button,
                R.id.background_button
        };

        int[] containerIds = {
                R.id.sticker_container,
                R.id.face_container,
                R.id.eyes_container,
                R.id.nose_container,
                R.id.mouth_container,
                R.id.hair_container,
                R.id.background_container
        };

        DrawingUIHelper.initializeDrawerButtons(this, buttonIds, containerIds);

// ✅ 스티커 클릭 시 DrawingView에 추가
        DrawingUIHelper.initializeStickerButtons(this, drawingView);

        pageIndicator = findViewById(R.id.page_indicator);
        myPageInfo = findViewById(R.id.my_page_info);
        progressInfo = findViewById(R.id.progress_info);

        drawingView = findViewById(R.id.drawing_view);
        drawingContainer = findViewById(R.id.drawing_container);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        pageUrls = new ArrayList<>();

        title = getIntent().getStringExtra("title");
        coverResId = getIntent().getIntExtra("coverResId", 0);
        totalPages = getIntent().getIntExtra("pageCount", 0);
        category = getIntent().getStringExtra("category");
        docId = getIntent().getStringExtra("docId");


        participantCount = getIntent().getIntExtra("participantCount", 1);
        myIndex = getIntent().getIntExtra("myIndex", 0);
        docId = getIntent().getStringExtra("docId");
        isTogether = getIntent().getBooleanExtra("isTogether", false);
        isWithFriend = getIntent().getBooleanExtra("isWithFriend", false);
        boolean isAlone = getIntent().getBooleanExtra("isAlone", false);

        if (docId == null || docId.isEmpty()) {
            docId = UUID.randomUUID().toString(); // 이것도 반드시 있어야 함
        }

        myPageIndices = new ArrayList<>();

        int basePages = totalPages / participantCount;
        int remainder = totalPages % participantCount;
        int start = myIndex * basePages + Math.min(myIndex, remainder);
        int end = start + basePages + (myIndex < remainder ? 1 : 0);
        for (int i = start; i < end; i++) myPageIndices.add(i);

        StringBuilder sb = new StringBuilder("내 페이지: ");
        for (int i = 0; i < myPageIndices.size(); i++) {
            sb.append(myPageIndices.get(i) + 1); // 사용자 기준 1부터
            if (i < myPageIndices.size() - 1) sb.append(", ");
        }
        myPageInfo.setText(sb.toString());

        String roomId = (docId != null && !docId.isEmpty()) ? "family_" + docId : "family_default";
        linesRef = FirebaseDatabase.getInstance().getReference("drawings").child(roomId).child("lines");

        drawingView.setOnStrokeCompleteListener(this);
        subscribeRemoteLines();


        btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        btnNext = (ImageButton) findViewById(R.id.btn_next);
        btnSave = (ImageButton) findViewById(R.id.btn_save);
        btnCancel = (ImageButton) findViewById(R.id.btn_cancel);



        btnNext.setOnClickListener(v -> saveCurrentPage(() -> {
            int targetPage = currentPage + 1;

            if (targetPage >= totalPages) {
                Toast.makeText(this, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show();
                return;
            }


            if (isTogether && !isWithFriend && myIndex == 0 && !myPageIndices.contains(targetPage)) {
                Toast.makeText(this, "다른 사람의 페이지로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }


            currentPage = targetPage;
            updatePage();
        }));

        btnPrev.setOnClickListener(v -> saveCurrentPage(() -> {
            int targetPage = currentPage - 1;

            if (targetPage < 0) {
                Toast.makeText(this, "첫 번째 페이지입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isTogether && !isWithFriend && myIndex == 0 && !myPageIndices.contains(targetPage)) {
                Toast.makeText(this, "다른 사람의 페이지로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }


            currentPage = targetPage;
            updatePage();
        }));




        btnSave.setOnClickListener(v -> saveCurrentPage(this::saveBookToFirestore));

        btnCancel.setOnClickListener(v -> {
            if (isTogether && completedPages != null) {
                boolean othersCompleted = false;
                for (Map.Entry<String, Boolean> entry : completedPages.entrySet()) {
                    int pageNum = Integer.parseInt(entry.getKey().replace("page_", ""));
                    if (!myPageIndices.contains(pageNum) && Boolean.TRUE.equals(entry.getValue())) {
                        othersCompleted = true;
                        break;
                    }
                }

                if (othersCompleted) {
                    Toast.makeText(this, "다른 사람이 작성한 이후에는 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            new AlertDialog.Builder(this)
                    .setTitle("작성 취소")
                    .setMessage("정말로 작성 중인 동화를 삭제할까요?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        if (docId != null && !docId.isEmpty()) {


                            String collection;
                            if (isWithFriend) {
                                collection = "friendTogetherStoriesFm";
                            } else if (isTogether) {
                                collection = "togetherStoriesFm";
                            } else {
                                collection = "familystory";
                            }

                            db.collection(collection).document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deleteAllPagesFromStorage(title, totalPages);
                                        Toast.makeText(this, "동화가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            finish();
                        }
                    })

                    .setNegativeButton("취소", null)
                    .show();
        });


        if (docId != null && !docId.isEmpty()) {
            loadBookFromFirestore(docId);
        } else {
            pages = new ArrayList<>();
            for (int i = 0; i < totalPages; i++) {
                pages.add(new StoryPage("", ""));
                pageUrls.add("");
            }
            updatePage();
        }


    }


    private void loadBookFromFirestore(String docId) {

        if (isTogether) {   //모두와 함께 모드
            db.collection("togetherStoriesFm").document(docId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (!document.exists()) {
                            Toast.makeText(this, "새 동화를 시작합니다.", Toast.LENGTH_SHORT).show();
                            initEmptyPages();
                            return;
                        }

                        title = document.getString("title");
                        category = document.getString("category");
                        Long coverL = document.getLong("coverResId");
                        coverResId = (coverL != null) ? coverL.intValue() : 0;

                        pageUrls = (List<String>) document.get("pageUrls");
                        if (pageUrls == null) pageUrls = new ArrayList<>();
                        totalPages = pageUrls.size();

                        Long pc = document.getLong("pageCount");
                        if (pc != null && pc.intValue() < totalPages) {
                            pageUrls = new ArrayList<>(pageUrls.subList(0, pc.intValue()));
                            totalPages = pc.intValue();
                        }

                        completedPages = (Map<String, Boolean>) document.get("completedPages");
                        if (completedPages == null) completedPages = new HashMap<>();

                        assignedUsers = (Map<String, String>) document.get("assignedUsers");
                        if (assignedUsers == null) assignedUsers = new HashMap<>();

                        String ownerId = document.getString("ownerId");
                        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (assignedUsers.isEmpty()) {
                            int basePages = totalPages / participantCount;
                            int remainder = totalPages % participantCount;
                            int start = 0;
                            int end = basePages + (0 < remainder ? 1 : 0);

                            for (int i = start; i < end; i++) {
                                assignedUsers.put("page_" + i, ownerId);
                            }

                            db.collection("togetherStoriesFm").document(docId)
                                    .update("assignedUsers", assignedUsers);
                        }

                        myPageIndices = new ArrayList<>();
                        for (Map.Entry<String, String> entry : assignedUsers.entrySet()) {
                            if (currentUid.equals(entry.getValue())) {
                                int pageIndex = Integer.parseInt(entry.getKey().replace("page_", ""));
                                myPageIndices.add(pageIndex);
                            }
                        }

                        boolean allCompleted = true;
                        for (int i = 0; i < totalPages; i++) {
                            Boolean completed = completedPages.get("page_" + i);
                            if (completed == null || !completed) {
                                allCompleted = false;
                                break;
                            }
                        }

                        if (allCompleted) {
                            myPageIndices.clear();
                            for (int i = 0; i < totalPages; i++) {
                                myPageIndices.add(i);
                            }
                        }

                        pages = new ArrayList<>();
                        for (int i = 0; i < totalPages; i++) {
                            String url = (i < pageUrls.size()) ? pageUrls.get(i) : "";
                            pages.add(new StoryPage("", url));
                        }

                        int done = 0;
                        for (Boolean b : completedPages.values()) {
                            if (b != null && b) done++;
                        }
                        int percent = (int) ((done * 100.0f) / totalPages);

                        if (progressInfo != null) {
                            progressInfo.setText("진행률: " + percent + "% (" + done + "/" + totalPages + ")");
                        } else {
                            Log.d("진행률", "progress_info는 현재 레이아웃에 없음 (혼자/친구 모드)");
                        }
                        if (allCompleted) {
                            btnCancel.setEnabled(false);
                            btnCancel.setAlpha(0.4f);
                            btnCancel.setOnClickListener(null);
                        }

                        if (!myPageIndices.isEmpty() && !currentUid.equals(ownerId)) {
                            currentPage = myPageIndices.get(0);
                        } else {
                            currentPage = 0;
                        }

                        updatePage();
                        Toast.makeText(this, "동화 불러오기 완료!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } else if (isWithFriend) {
            //친구와 함께 모드
            Log.d("docId", "받아온 docId = " + docId);
            db.collection("friendTogetherStoriesFm").document(docId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            title = document.getString("title");
                            category = document.getString("category");
                            coverResId = document.getLong("coverResId") != null
                                    ? document.getLong("coverResId").intValue() : 0;

                            pageUrls = (List<String>) document.get("pageUrls");
                            if (pageUrls == null) pageUrls = new ArrayList<>();
                            totalPages = pageUrls.size();

                            completedPages = (Map<String, Boolean>) document.get("completedPages");
                            if (completedPages == null) completedPages = new HashMap<>();

                            assignedUsers = (Map<String, String>) document.get("assignedUsers");
                            if (assignedUsers == null) assignedUsers = new HashMap<>();

                            List<String> ownerIds = (List<String>) document.get("ownerId");
                            String ownerId = (ownerIds != null && !ownerIds.isEmpty()) ? ownerIds.get(0) : "";

                            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            if (assignedUsers.isEmpty()) {
                                int basePages = totalPages / participantCount;
                                int remainder = totalPages % participantCount;
                                int start = 0;
                                int end = basePages + (0 < remainder ? 1 : 0);

                                for (int i = start; i < end; i++) {
                                    assignedUsers.put("page_" + i, ownerId);
                                }

                                db.collection("friendTogetherStoriesFm").document(docId)
                                        .update("assignedUsers", assignedUsers);

                            }

                            myPageIndices = new ArrayList<>();
                            for (Map.Entry<String, String> entry : assignedUsers.entrySet()) {
                                if (currentUid.equals(entry.getValue())) {
                                    int pageIndex = Integer.parseInt(entry.getKey().replace("page_", ""));
                                    myPageIndices.add(pageIndex);
                                }
                            }

                            pages = new ArrayList<>();
                            for (int i = 0; i < totalPages; i++) {
                                String url = (i < pageUrls.size()) ? pageUrls.get(i) : "";
                                pages.add(new StoryPage("", url));
                            }

                            int done = 0;
                            for (Boolean b : completedPages.values()) {
                                if (b != null && b) done++;
                            }
                            int percent = (int) ((done * 100.0f) / totalPages);

                            if (progressInfo != null) {
                                progressInfo.setText("진행률: " + percent + "% (" + done + "/" + totalPages + ")");
                            } else {
                                Log.d("진행률", "progress_info는 현재 레이아웃에 없음 (혼자/친구 모드)");
                            }
                            if (!myPageIndices.isEmpty()) {
                                if (!currentUid.equals(ownerId)) {
                                    currentPage = myPageIndices.get(0);
                                } else {
                                    currentPage = 0;
                                }
                            } else {
                                currentPage = 0;
                            }
                            updatePage();
                            Toast.makeText(this, "동화 불러오기 완료!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "새 동화를 시작합니다.", Toast.LENGTH_SHORT).show();
                            pages = new ArrayList<>();
                            pageUrls = new ArrayList<>();
                            for (int i = 0; i < totalPages; i++) {
                                pages.add(new StoryPage("", ""));
                                pageUrls.add("");
                            }
                            updatePage();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {   //혼자 모드

            db.collection("familystory").document(docId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (!document.exists()) {
                            Toast.makeText(this, "새 동화를 시작합니다.", Toast.LENGTH_SHORT).show();
                            initEmptyPages();
                            return;
                        }

                        title = document.getString("title");
                        category = document.getString("category");
                        Long coverL = document.getLong("coverResId");
                        coverResId = (coverL != null) ? coverL.intValue() : 0;

                        pageUrls = (List<String>) document.get("pageUrls");
                        if (pageUrls == null) pageUrls = new ArrayList<>();
                        totalPages = pageUrls.size();

                        pages = new ArrayList<>();
                        for (String url : pageUrls) {
                            pages.add(new StoryPage("", url));
                        }

                        currentPage = 0;
                        updatePage();
                        Toast.makeText(this, "동화 불러오기 완료!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


    private void initEmptyPages() {
        pages = new ArrayList<>();
        pageUrls = new ArrayList<>();
        for (int i = 0; i < totalPages; i++) {
            pages.add(new StoryPage("", ""));
            pageUrls.add("");
        }
        currentPage = 0;
        updatePage();
    }





    private void updatePage() {
        if (pages == null || pages.isEmpty() || currentPage >= pages.size()) return;

        while (pageUrls.size() < totalPages) {
            pageUrls.add("");
        }
        if (pageUrls.size() > totalPages) {
            pageUrls = pageUrls.subList(0, totalPages);
        }

        StoryPage page = pages.get(currentPage);
        drawingView.clear();
        drawingContainer.removeViews(1, drawingContainer.getChildCount() - 1);
        pageIndicator.setText((currentPage + 1) + " / " + totalPages + " 페이지");


        boolean editable = false;

        if (isTogether) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String assignedUid = null;
            if (assignedUsers != null) {
                assignedUid = assignedUsers.get("page_" + currentPage);
            }
            boolean isCompleted = completedPages != null &&
                    Boolean.TRUE.equals(completedPages.get("page_" + currentPage));
            editable = assignedUid != null && uid.equals(assignedUid) && !isCompleted;
        } else  if (isWithFriend) {
            editable = true;                      // 친구 모드는 전 페이지 편집 허용
        } else {
            editable = myPageIndices.contains(currentPage);  // 혼자 모드
        }


        drawingView.setEnabled(editable);
        btnSave.setEnabled(editable);



        if (!pageUrls.isEmpty() && currentPage < pageUrls.size()) {
            String imageUrl = pageUrls.get(currentPage);
            if (!imageUrl.isEmpty()) {
                Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                drawingView.setBackgroundBitmap(resource);
                            }
                        });
            }
        }
    }

    private void saveCurrentPage(Runnable onSuccess) {

        DrawingUIHelper.hideAllHandles();

        Bitmap bitmap = getBitmapFromView(drawingContainer);
        if (bitmap == null) {
            Toast.makeText(this, "저장할 그림이 없습니다.", Toast.LENGTH_SHORT).show();
            onSuccess.run();
            return;
        }

        String folderName = "story_drawings/" + title;
        String fileName   = "page_" + (currentPage + 1) + ".png";
        StorageReference storageRef = storage.getReference(folderName + "/" + fileName);
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Uri fileUri = Uri.fromFile(file);
            storageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        if (pageUrls.size() < totalPages) {
                            while (pageUrls.size() < totalPages) {
                                pageUrls.add("");
                            }
                        } else if (pageUrls.size() > totalPages) {
                            // 잘못 늘어난 경우 잘라냄
                            pageUrls = new ArrayList<>(pageUrls.subList(0, totalPages));
                        }

                        if (currentPage < totalPages) {
                            pageUrls.set(currentPage, uri.toString());
                        }
                        Log.d("saveCurrentPage", "Updated pageUrls: " + pageUrls.toString());

                        Toast.makeText(this, "페이지 저장 완료", Toast.LENGTH_SHORT).show();
                        onSuccess.run();
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        onSuccess.run();
                    });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "파일 저장 중 오류 발생", Toast.LENGTH_SHORT).show();
            onSuccess.run();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        if (view.getBackground() != null) {
            view.getBackground().draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private void saveBookToFirestore() {
        if (isTogether) {
            if (!myPageIndices.contains(currentPage)) {
                Toast.makeText(this, "다른 사람의 페이지는 저장할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            // ✅ 내 분량만 검사
            for (int pageIndex : myPageIndices) {
                if (pageUrls.size() <= pageIndex || pageUrls.get(pageIndex).isEmpty()) {
                    Toast.makeText(this, "내 페이지를 모두 저장한 후 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // 🔄 내 페이지 저장 완료 → 업데이트
            Map<String, Object> update = new HashMap<>();
            update.put("pageUrls", pageUrls);
            update.put("completedPages.page_" + currentPage, true); // 현재 페이지 완료 표시

            db.collection("togetherStoriesFm").document(docId)
                    .update(update)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "동화 저장 완료!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else if (isWithFriend) {
            if (pageUrls.contains("")) {
                Toast.makeText(this, "모든 페이지를 저장한 후 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (docId == null || docId.isEmpty()) {
                docId = UUID.randomUUID().toString();
            }

            ArrayList<String> ownerIds = new ArrayList<>();
            ownerIds.add(uid);

            if (selectedFriendUids != null && !selectedFriendUids.isEmpty()) {
                for (String friendUid : selectedFriendUids) {
                    if (!friendUid.equals(uid)) {
                        ownerIds.add(friendUid);
                    }
                }
            }

            Log.d("디버그", "Firestore에 저장할 ownerIds: " + ownerIds.toString());
            Log.d("디버그", "Firestore에 저장할 docId: " + docId.toString());

            int participantCount = ownerIds.size();

            Map<String, Object> bookData = new HashMap<>();
            bookData.put("title", title);
            bookData.put("category", category);
            bookData.put("coverResId", coverResId);
            bookData.put("pageUrls", pageUrls);
            bookData.put("ownerId", ownerIds);
            bookData.put("participantCount", participantCount);

            db.collection("friendTogetherStoriesFm").document(docId)
                    .set(bookData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "동화 저장 완료!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        }
        else {
            // ✅ 혼자인 경우 기존 로직 유지
            if (pageUrls.contains("")) {
                Toast.makeText(this, "모든 페이지를 저장한 후 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (docId == null || docId.isEmpty()) {
                docId = UUID.randomUUID().toString();
            }

            List<String> ownerIds = new ArrayList<>();
            ownerIds.add(uid);

            int participantCount = 1;

            Map<String, Object> bookData = new HashMap<>();
            bookData.put("title", title);
            bookData.put("category", category);
            bookData.put("coverResId", coverResId);
            bookData.put("pageUrls", pageUrls);
            bookData.put("ownerId", ownerIds);
            bookData.put("participantCount", participantCount);

            db.collection("familystory").document(docId)
                    .set(bookData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "동화 저장 완료!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }



    @Override
    public void onStrokeComplete(Line line) {
        String key = linesRef.push().getKey();
        if (key != null) {
            linesRef.child(key).setValue(line);
        }
    }

    private void subscribeRemoteLines() {
        linesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Line remote = snapshot.getValue(Line.class);
                drawingView.drawRemoteLine(remote);
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.story_edit_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_change_color) {
            DrawingUIHelper.setupColorButtons(this, drawingView);
            return true;
        } else if (id == R.id.menu_add_text) {
            DrawingUIHelper.showTextInputDialog(this, drawingContainer);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}