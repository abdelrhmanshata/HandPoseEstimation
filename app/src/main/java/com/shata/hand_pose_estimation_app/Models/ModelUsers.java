package com.shata.hand_pose_estimation_app.Models;

import java.io.Serializable;

public class ModelUsers implements Serializable {

    String UID , userType;

    public ModelUsers() {
    }

    public ModelUsers(String UID, String userType) {
        this.UID = UID;
        this.userType = userType;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
