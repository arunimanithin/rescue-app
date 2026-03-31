
package com.rescueapp.core;

public class StrayAnimal {
    private String animalId;
    private String name;
    private int age;
    private String gender;
    private String specifications;
    private String photoUrl;
    private String medReport;
    private String status;

    public StrayAnimal() {
    }

    public StrayAnimal(String animalId, String name, int age, String gender, String specifications, String photoUrl, String medReport, String status) {
        this.animalId = animalId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.specifications = specifications;
        this.photoUrl = photoUrl;
        this.medReport = medReport;
        this.status = status;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getMedReport() {
        return medReport;
    }

    public void setMedReport(String medReport) {
        this.medReport = medReport;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // A simple method to return information summary
    public String getInfo() {
        return "\nAnimal Details:" +
               "\n--------------------------------" +
               "\nAnimal ID       : " + animalId +
               "\nSpecifications  : " + specifications +
               "\nMedical Report  : " + medReport +
               "\nCurrent Status  : " + status +
               "\n--------------------------------";
    }


    @Override
    public String toString() {
        return "StrayAnimal{" +
                "animalId='" + animalId + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", specifications='" + specifications + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", medReport='" + medReport + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
