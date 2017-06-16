package no.rustelefonen.hap.intro;

import java.io.Serializable;
import java.util.Calendar;

import no.rustelefonen.hap.entities.User;

/**
 * Created by simenfonnes on 16.06.2017.
 */

public class PrivacyPojo implements Serializable{

    private boolean agreedToParticipate;
    private int age;
    private User.Gender gender;
    private String county;
    private String userType;
    private Calendar startDate;

    public boolean isAgreedToParticipate() {
        return agreedToParticipate;
    }

    public void setAgreedToParticipate(boolean agreedToParticipate) {
        this.agreedToParticipate = agreedToParticipate;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public User.Gender getGender() {
        return gender;
    }

    public void setGender(User.Gender gender) {
        this.gender = gender;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "PrivacyPojo{" +
                "agreedToParticipate=" + agreedToParticipate +
                ", age=" + age +
                ", gender=" + gender +
                ", county='" + county + '\'' +
                ", userType='" + userType + '\'' +
                ", startDate=" + startDate +
                '}';
    }
}
