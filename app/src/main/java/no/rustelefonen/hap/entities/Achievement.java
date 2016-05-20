package no.rustelefonen.hap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.rustelefonen.hap.R;


/**
 * Created by martinnikolaisorlie on 27/01/16.
 */
@Data
@NoArgsConstructor
public class Achievement implements Parcelable {
    public enum Type {
        HEALTH, FINANCE, MILESTONE, MINOR_MILESTONE
    }
    public static final String ACHIEVEMENT_ID_COLOUMN = "ACHIEVEMENT_ID";
    public static final String ACHIEVEMENT_TYPE_COLOUMN = "ACHIEVEMENT_TYPE";

    @DatabaseField(generatedId = true, columnName = ACHIEVEMENT_ID_COLOUMN)
    private int id;

    @DatabaseField(canBeNull = false)
    private String title;

    @DatabaseField(canBeNull = false, columnName = ACHIEVEMENT_TYPE_COLOUMN)
    private Type type;

    @DatabaseField
    private int pointsRequired;

    @DatabaseField (canBeNull = false)
    private String description;

    public int getImageId(boolean isComplete){
        switch (type){
            case FINANCE:
                return isComplete ? R.drawable.finance : R.drawable.finance_white;
            case HEALTH:
                return isComplete ? R.drawable.health : R.drawable.health_white;
            case MILESTONE:
                return isComplete ? R.drawable.milestone : R.drawable.milestone_white;
            case MINOR_MILESTONE:
                return isComplete ? R.drawable.minor_milestone : R.drawable.minor_milestone_white;
        }
        return 0;
    }

    public double getProgress(User user){
        if(user == null) return 0;

        switch (type){
            case MILESTONE:
            case MINOR_MILESTONE:
            case HEALTH:
                double progressBeforeReset = user.getSecondsLastedBeforeLastReset() / (double) pointsRequired;
                if (progressBeforeReset >= 1) return progressBeforeReset;
                else return user.secondsSinceStarted() / (double) pointsRequired;
            case FINANCE:
                double moneyProgressBeforeReset = user.totalMoneySavedBeforeReset() / (double) pointsRequired;
                if (moneyProgressBeforeReset >= 1) return moneyProgressBeforeReset;
                return user.totalMoneySaved() / (double) pointsRequired;
            default:
                return 0;
        }
    }

    public boolean isComplete(User user) {
        return getProgress(user) >= 1;
    }

    public double secondsToCompletion(User user) {
        switch (type){
            case MILESTONE:
            case MINOR_MILESTONE:
            case HEALTH:
                double seconds = user.getSecondsLastedBeforeLastReset() > 0
                        ? Math.max(user.getSecondsLastedBeforeLastReset(), user.secondsSinceStarted())
                        : user.secondsSinceStarted();
                return pointsRequired - seconds;

            case FINANCE:
                double moneySaved = user.totalMoneySaved();
                double progressBeforeReset = user.totalMoneySavedBeforeReset() / (double) pointsRequired;
                if(progressBeforeReset >= 1) moneySaved = user.totalMoneySavedBeforeReset();

                return (pointsRequired - moneySaved) / user.moneySavedPerSecond();
            default:
                return 0;
        }
    }

    // Parcel methods
    public static final Parcelable.Creator<Achievement> CREATOR = new Parcelable.Creator<Achievement>() {
        public Achievement createFromParcel(Parcel in) {
            return new Achievement(in);
        }

        public Achievement[] newArray(int size) {
            return new Achievement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(pointsRequired);
        out.writeString(title);
        out.writeString(description);
        out.writeString(type == null ? "" : type.toString());
    }

    private Achievement(Parcel in) {
        setId(in.readInt());
        setPointsRequired(in.readInt());
        setTitle(in.readString());
        setDescription(in.readString());
        String type = in.readString();
        setType(type.equals("") ? null : Type.valueOf(type));
    }
}
