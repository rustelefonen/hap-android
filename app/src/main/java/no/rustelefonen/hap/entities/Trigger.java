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
public class Trigger implements Parcelable {
    public static final String TRIGGER_ID_COLOUMN = "TRIGGER_ID";
    public static final String TRIGGER_TITLE_COLOUMN = "TRIGGER_TITLE";

    @DatabaseField(generatedId = true, columnName = TRIGGER_ID_COLOUMN)
    private int id;

    @DatabaseField
    private int imageId;

    @DatabaseField(columnName = TRIGGER_TITLE_COLOUMN)
    private String title;

    @DatabaseField
    private int color;

    // Parcel methods
    public static final Parcelable.Creator<Trigger> CREATOR = new Parcelable.Creator<Trigger>() {
        public Trigger createFromParcel(Parcel in) {
            return new Trigger(in);
        }

        public Trigger[] newArray(int size) {
            return new Trigger[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(imageId);
        out.writeInt(color);
        out.writeString(title);
    }

    private Trigger(Parcel in) {
        setId(in.readInt());
        setImageId(in.readInt());
        setColor(in.readInt());
        setTitle(in.readString());
    }
}
