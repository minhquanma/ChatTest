package com.mmq.chattest;

/**
 * Created by Jon on 7/27/2017.
 */

public class Messages {

    private boolean isMe;
    private String key;
    private String uid;
    private String sender;
    private String message;
    private String avatar;

    public Messages() {
    }

    public Messages(String uid, String key, String sender, String message, String avatar) {
        this.uid = uid;
        this.key = key;
        this.sender = sender;
        this.message = message;
        this.avatar = avatar;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
