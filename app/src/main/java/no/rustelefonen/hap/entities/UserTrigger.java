package no.rustelefonen.hap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Fredrik on 27.02.2016.
 */
@Data
@NoArgsConstructor
public class UserTrigger implements Parcelable{
    public enum Type{ RESISTED, SMOKED }

    public static final String USER_TRIGGER_ID_COLOUMN = "USER_TRIGGER_ID";
    public static final String TRIGGER_TYPE_COLOUMN = "TRIGGER_TYPE";
    public static final String USER_FK_COLOUMN = "USER_FK";
    public static final String TRIGGER_FK_COLOUMN = "TRIGGER_FK";

    @DatabaseField(generatedId = true, columnName = USER_TRIGGER_ID_COLOUMN)
    private int id;

    @DatabaseField(columnName = TRIGGER_TYPE_COLOUMN)
    private Type type;

    @DatabaseField(foreign = true, columnName = USER_FK_COLOUMN)
    private User user;

    @DatabaseField(foreign = true, columnName = TRIGGER_FK_COLOUMN)
    private Trigger trigger;

    @DatabaseField
    private int count = 1;

    @Override
    public String toString() {
        return "UserTrigger{" +
                "type=" + type +
                ", id=" + id +
                ", trigger=" + trigger +
                ", count=" + count +
                '}';
    }

    // Parcel methods
    public static final Parcelable.Creator<UserTrigger> CREATOR = new Parcelable.Creator<UserTrigger>() {
        public UserTrigger createFromParcel(Parcel in) {
            return new UserTrigger(in);
        }

        public UserTrigger[] newArray(int size) {
            return new UserTrigger[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(type != null ? type.toString() : "");
        out.writeParcelable(trigger, flags);
        out.writeInt(count);
    }

    private UserTrigger(Parcel in) {
        setId(in.readInt());
        String type = in.readString();
        setType(type.equals("") ? null : Type.valueOf(type));
        setTrigger((Trigger) in.readParcelable(Trigger.class.getClassLoader()));
        setCount(in.readInt());
    }
}