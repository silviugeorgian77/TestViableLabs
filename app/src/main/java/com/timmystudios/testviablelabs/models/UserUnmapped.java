package com.timmystudios.testviablelabs.models;

import com.google.gson.annotations.SerializedName;

public class UserUnmapped {

    @SerializedName("name")
    public Name name;
    @SerializedName("dob")
    public Dob dob;
    @SerializedName("email")
    public String email;
    @SerializedName("nationality")
    public String nationality;
    @SerializedName("picture")
    public Picture picture;

    public static class Name {
        @SerializedName("first")
        public String first;
        @SerializedName("second")
        public String second;
    }

    public static class Dob {
        @SerializedName("age")
        public int age;
    }

    public static class Picture {
        @SerializedName("large")
        public String large;
        @SerializedName("medium")
        public String medium;
        @SerializedName("thumbnail")
        public String thumbnail;
    }

}
