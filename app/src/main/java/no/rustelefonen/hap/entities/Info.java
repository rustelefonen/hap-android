package no.rustelefonen.hap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by martinnikolaisorlie on 27/01/16.
 */
@Data
@NoArgsConstructor
public class Info implements Parcelable {
    public static final String INFO_ID_COLOUMN = "INFO_ID";
    public static final String INFO_CATEGORY_ID = "INFO_CATEGORY_ID";
    public static final String INFO_TITLE_COLOUMN = "INFO_TITLE";

    @DatabaseField(generatedId = true, columnName = INFO_ID_COLOUMN)
    private int id;

    @DatabaseField(foreign = true, columnName = INFO_CATEGORY_ID)
    private Category category;

    @DatabaseField(columnName = INFO_TITLE_COLOUMN)
    private String title;

    @DatabaseField
    private String htmlContent;

    @Override
    public String toString() {
        return "Info{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", htmlContent='" + htmlContent + '\'' +
                '}';
    }

    // Parcel methods
    public static final Parcelable.Creator<Info> CREATOR = new Parcelable.Creator<Info>() {
        public Info createFromParcel(Parcel in) {
            return new Info(in);
        }

        public Info[] newArray(int size) {
            return new Info[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(title);
        out.writeString(htmlContent);
    }

    private Info(Parcel in) {
        setId(in.readInt());
        setTitle(in.readString());
        setHtmlContent(in.readString());
    }
}