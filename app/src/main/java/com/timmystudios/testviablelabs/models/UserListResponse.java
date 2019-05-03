package com.timmystudios.testviablelabs.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserListResponse {

    public List<Result> results;

    public static class Result {
        @SerializedName("name")
        public Name name;
        @SerializedName("dob")
        public Dob dob;
        @SerializedName("email")
        public String email;
        @SerializedName("nat")
        public String nat;
        @SerializedName("picture")
        public Picture picture;
    }

    public static class Name {
        @SerializedName("first")
        public String first;
        @SerializedName("last")
        public String last;
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
