package com.timmystudios.testviablelabs.models;

public class User {

    private String firstName;
    private String lastName;
    private int age;
    private String email;
    private String nationality;
    private String pictureThumbnailUrl;
    private String pictureMediumUrl;
    private String pictureLargeUrl;

    public User() {
    }

    public User(String firstName,
                String lastName,
                int age,
                String email,
                String nationality,
                String pictureThumbnailUrl,
                String pictureMediumUrl,
                String pictureLargeUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.email = email;
        this.nationality = nationality;
        this.pictureThumbnailUrl = pictureThumbnailUrl;
        this.pictureMediumUrl = pictureMediumUrl;
        this.pictureLargeUrl = pictureLargeUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPictureThumbnailUrl() {
        return pictureThumbnailUrl;
    }

    public void setPictureThumbnailUrl(String pictureThumbnailUrl) {
        this.pictureThumbnailUrl = pictureThumbnailUrl;
    }

    public String getPictureMediumUrl() {
        return pictureMediumUrl;
    }

    public void setPictureMediumUrl(String pictureMediumUrl) {
        this.pictureMediumUrl = pictureMediumUrl;
    }

    public String getPictureLargeUrl() {
        return pictureLargeUrl;
    }

    public void setPictureLargeUrl(String pictureLargeUrl) {
        this.pictureLargeUrl = pictureLargeUrl;
    }
}
