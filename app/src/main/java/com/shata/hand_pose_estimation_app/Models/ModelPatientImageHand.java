package com.shata.hand_pose_estimation_app.Models;

public class ModelPatientImageHand {

    String uID;
    String ImageUri;

    public ModelPatientImageHand() {
    }

    public ModelPatientImageHand(String uID, String imageUri) {
        this.uID = uID;
        ImageUri = imageUri;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getImageUri() {
        return ImageUri;
    }

    public void setImageUri(String imageUri) {
        ImageUri = imageUri;
    }
}
