package com.example.madgroupproject.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

//all info in userProfile include the Profile pic
@Entity(tableName = "user_profile")
public class UserProfile {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String email;
    private String phone;
    private String birthday;
    private String weight;
    private String height;
    private String profileImageUri;




    // Constructor
    public UserProfile(String name, String email, String phone, String birthday, String weight, String height,String profileImageUri) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthday = birthday;
        this.weight = weight;
        this.height = height;
        this.profileImageUri = profileImageUri;

    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }

    public String getProfileImageUri() { return profileImageUri; }
    public void setProfileImageUri(String uri) { this.profileImageUri = uri; }

}
