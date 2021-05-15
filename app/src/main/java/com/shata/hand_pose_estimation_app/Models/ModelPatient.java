package com.shata.hand_pose_estimation_app.Models;

import java.io.Serializable;

public class ModelPatient implements Serializable {

    String patientID = "",
            patientName = "",
            patientAge = "",
            patientEmail = "",
            patientPhone = "",
            patientGender = "",
            patientImageUri = "";

    public ModelPatient() {
    }

    public ModelPatient(String patientID, String patientName, String patientAge, String patientEmail, String patientPhone, String patientGender, String patientImageUri) {
        this.patientID = patientID;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientGender = patientGender;
        this.patientImageUri = patientImageUri;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPatientImageUri() {
        return patientImageUri;
    }

    public void setPatientImageUri(String patientImageUri) {
        this.patientImageUri = patientImageUri;
    }
}
