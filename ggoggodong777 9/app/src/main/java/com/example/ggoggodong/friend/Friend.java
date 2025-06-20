package com.example.ggoggodong.friend;

public class Friend {
    private String uid;
    private String name;
    private String profileImageUrl;

    public Friend(String uid, String name, String profileImageUrl) {
        this.uid = uid;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getProfileImageUrl() { return profileImageUrl; }
}
