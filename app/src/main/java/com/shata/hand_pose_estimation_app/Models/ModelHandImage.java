package com.shata.hand_pose_estimation_app.Models;

import java.io.Serializable;

public class ModelHandImage implements Serializable {

    String
        imageID,
        imageUri,
        imageDate;

    public ModelHandImage() {
    }

    public ModelHandImage(String imageID, String imageUri, String imageDate) {
        this.imageID = imageID;
        this.imageUri = imageUri;
        this.imageDate = imageDate;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getImageDate() {
        return imageDate;
    }

    public void setImageDate(String imageDate) {
        this.imageDate = imageDate;
    }
}
