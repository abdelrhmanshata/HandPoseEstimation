package com.shata.hand_pose_estimation_app.Models;

import java.io.Serializable;

public class ModelDataUser implements Serializable {
    String userID = "",
            userName = "",
            userAge = "",
            userEmail = "",
            userPassword = "",
            userPhone = "",
            userGender = "",
            userImageUri = "",
            userType = "";

    public ModelDataUser() {
    }

    public ModelDataUser(String userID, String userName, String userAge, String userEmail, String userPassword, String userPhone, String userGender, String userImageUri, String userType) {
        this.userID = userID;
        this.userName = userName;
        this.userAge = userAge;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userPhone = userPhone;
        this.userGender = userGender;
        this.userImageUri = userImageUri;
        this.userType = userType;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAge() {
        return userAge;
    }

    public void setUserAge(String userAge) {
        this.userAge = userAge;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserImageUri() {
        return userImageUri;
    }

    public void setUserImageUri(String userImageUri) {
        this.userImageUri = userImageUri;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "ModelDataUser{" +
                "userID='" + userID + '\'' +
                ", userName='" + userName + '\'' +
                ", userAge='" + userAge + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userGender='" + userGender + '\'' +
                ", userImageUri='" + userImageUri + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
