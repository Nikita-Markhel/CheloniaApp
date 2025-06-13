package com.example.chelonia.information;

public class User {
    private String firstName;
    private String secondName;
    private String avatarUri;

    public User(String firstName, String secondName, String avatarUri){
        this.firstName = firstName;
        this.secondName = secondName;
        this.avatarUri = avatarUri;
    }

    public User(String firstName, String secondName){
        this(firstName, secondName, "");
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getSecondName() {
        return secondName;
    }
    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }
    public String getAvatarUri() {
        return avatarUri;
    }
    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }
}
