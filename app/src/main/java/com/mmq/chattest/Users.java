package com.mmq.chattest;

import java.io.Serializable;

/**
 * Created by mmq on 02/08/2017.
 */

public class Users implements Serializable {

    private String uid;
    private String account;
    private String displayName;
    private String avatar;
    private String birthday;
    private boolean gender;

    public Users() {
    }

    public Users(String uid, String account, String displayName, String avatar, boolean gender) {
        this.uid = uid;
        this.account = account;
        this.displayName = displayName;
        this.avatar = avatar;
        this.gender = gender;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String stringGender() {
        return isGender() ? "Male" : "Female";
    }
}
