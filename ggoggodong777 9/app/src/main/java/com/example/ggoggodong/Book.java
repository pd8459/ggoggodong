package com.example.ggoggodong;

import java.util.List;

public class Book {
    private String title;
    private String content;
    private String userEmail;
    private boolean isCreateButton = false;
    private boolean isTogether = false;
    private String docId; // Firestore 문서 ID
    private boolean friend;

    // Firestore 저장/로딩용 추가 필드들
    private String category;
    private int coverResId;
    private List<String> pageUrls;
    private List<String> ownerId;
    private int participantCount;
    private int pageCount;

    // 기본 생성자 (Firestore에서 꼭 필요)
    public Book() {
    }

    // 생성자 (필요시)
    public Book(String title, String content, String userEmail) {
        this.title = title;
        this.content = content;
        this.userEmail = userEmail;
        this.isCreateButton = false;
    }

    // --- getter / setter ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isCreateButton() {
        return isCreateButton;
    }

    public void setCreateButton(boolean createButton) {
        isCreateButton = createButton;
    }

    public boolean isTogether() {
        return isTogether;
    }

    public void setTogether(boolean together) {
        isTogether = together;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    // Firestore 필드들
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCoverResId() {
        return coverResId;
    }

    public void setCoverResId(int coverResId) {
        this.coverResId = coverResId;
    }

    public List<String> getPageUrls() {
        return pageUrls;
    }

    public void setPageUrls(List<String> pageUrls) {
        this.pageUrls = pageUrls;
    }

    public List<String> getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(List<String> ownerId) {
        this.ownerId = ownerId;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
