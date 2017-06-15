package no.rustelefonen.hap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Iterables;
import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * Created by martinnikolaisorlie on 27/01/16.
 */
@Data
public class User implements Parcelable{
    public enum Gender { MALE, FEMALE, OTHER }

    public static final String USER_ID_COLOUMN = "USER_ID";

    @DatabaseField(generatedId = true, columnName = USER_ID_COLOUMN)
    private int id;

    @DatabaseField
    private int age;

    @DatabaseField
    private Gender gender;

    @DatabaseField
    private double secondsLastedBeforeLastReset;

    @DatabaseField
    private double moneySpentPerDayOnHash;

    @DatabaseField(canBeNull = false)
    private Date startDate;

    @DatabaseField
    private String county;

    @DatabaseField
    private String userType;

    @DatabaseField
    private Date appRegistered;

    @DatabaseField
    private Date surveyRegistered;

    @DatabaseField
    private Date secondSurveyRegistered;

    @DatabaseField
    private Date thirdSurveyRegistered;

    private List<UserTrigger> resistedTriggers;
    private List<UserTrigger> smokedTriggers;
    private List<UserTrigger> unSavedTriggers;

    public User(){
        startDate = new Date();
        setResistedTriggers(new ArrayList<UserTrigger>());
        setSmokedTriggers(new ArrayList<UserTrigger>());
        setUnSavedTriggers(new ArrayList<UserTrigger>());
    }

    public double secondsSinceStarted(){
        return (Calendar.getInstance().getTimeInMillis() - getStartDate().getTime()) / 1000.0;
    }

    public double totalMoneySaved() {
        return moneySavedPerSecond() * secondsSinceStarted();
    }

    public double totalMoneySavedBeforeReset() {
        return moneySavedPerSecond() * secondsLastedBeforeLastReset;
    }

    public double moneySavedPerSecond() {
        return (moneySpentPerDayOnHash / 86400.0);
    }

    public int daysSinceStarted(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 4); //at 4 o'clock a new day starts

        int seconds = (int) ((Calendar.getInstance().getTimeInMillis() - cal.getTimeInMillis()) / 1000.0);
        return seconds / 86400;
    }

    public void addTrigger(Trigger trigger, UserTrigger.Type type){
        if(incrementTriggerCountIfExisting(trigger, type)) return;

        UserTrigger userTrigger = new UserTrigger();
        userTrigger.setUser(this);
        userTrigger.setTrigger(trigger);
        userTrigger.setType(type);
        if(type == UserTrigger.Type.RESISTED) resistedTriggers.add(userTrigger);
        else smokedTriggers.add(userTrigger);

        unSavedTriggers.add(userTrigger);
    }

    private boolean incrementTriggerCountIfExisting(Trigger trigger, UserTrigger.Type type){
        List<UserTrigger> triggers = (type == UserTrigger.Type.RESISTED) ? getResistedTriggers() : getSmokedTriggers();

        boolean alreadyOnUser = false;
        for(UserTrigger triggerOnUser : triggers){
            if(!triggerOnUser.getTrigger().equals(trigger)) continue;
            alreadyOnUser = true;
            triggerOnUser.setCount(triggerOnUser.getCount() + 1);
            unSavedTriggers.add(triggerOnUser);
            break;
        }
        return alreadyOnUser;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", age=" + age +
                ", gender=" + gender +
                ", secondsLastedBeforeLastReset=" + secondsLastedBeforeLastReset +
                ", pricePerGram=" + moneySpentPerDayOnHash +
                ", startDate=" + startDate +
                ", county='" + county + '\'' +
                ", userType='" + userType + '\'' +
                ", appRegistered=" + appRegistered +
                ", surveyRegistered=" + surveyRegistered +
                ", secondSurveyRegistered=" + secondSurveyRegistered +
                ", thirdSurveyRegistered=" + thirdSurveyRegistered +
                '}';
    }

    public String getGenderAsString(){
        return gender != null ? gender.toString() : null;
    }

    //Parcelable methods
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(age);
        out.writeString(gender != null ? gender.toString() : "");
        out.writeString(userType != null ? userType : "");
        out.writeDouble(secondsLastedBeforeLastReset);
        out.writeDouble(moneySpentPerDayOnHash);
        out.writeLong(startDate.getTime());
        out.writeLong(appRegistered != null ? appRegistered.getTime() : 0);
        out.writeLong(surveyRegistered != null ? surveyRegistered.getTime() : 0);
        out.writeLong(secondSurveyRegistered != null ? secondSurveyRegistered.getTime() : 0);
        out.writeLong(thirdSurveyRegistered != null ? thirdSurveyRegistered.getTime() : 0);
        out.writeString(county);
        out.writeList(resistedTriggers);
        out.writeList(smokedTriggers);
        out.writeList(unSavedTriggers);
    }

    private User(Parcel in) {
        setId(in.readInt());
        setAge(in.readInt());
        String genderString = in.readString();
        String userTypeString = in.readString();
        setGender(genderString.equals("") ? null : Gender.valueOf(genderString));
        setSecondsLastedBeforeLastReset(in.readDouble());
        setMoneySpentPerDayOnHash(in.readDouble());
        setStartDate(new Date(in.readLong()));
        setAppRegistered(new Date(in.readLong()));
        setSurveyRegistered(new Date(in.readLong()));
        setSecondSurveyRegistered(new Date(in.readLong()));
        setThirdSurveyRegistered(new Date(in.readLong()));
        setCounty(in.readString());
        setUserType(userTypeString);

        resistedTriggers = new ArrayList<>();
        in.readList(resistedTriggers, UserTrigger.class.getClassLoader());

        smokedTriggers = new ArrayList<>();
        in.readList(smokedTriggers, UserTrigger.class.getClassLoader());

        unSavedTriggers = new ArrayList<>();
        in.readList(unSavedTriggers, UserTrigger.class.getClassLoader());

        for(UserTrigger userTrigger : Iterables.concat(smokedTriggers, resistedTriggers)){
            userTrigger.setUser(this);
        }
    }
}