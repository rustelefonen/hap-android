package no.rustelefonen.hap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Created by martinnikolaisorlie on 16/02/16.
 */
@Data
public class Category implements Parcelable {
    public static final String CATEGORY_ID_COLOUMN = "CATEGORY_ID";
    public static final String CATEGORY_ORDER_FIELD = "CATEGORY_ORDER";
    public static final String CATEGORY_VERSION_FIELD = "CATEGORY_VERSION";
    public static final String CATEGORY_TITLE_COLOUMN = "CATEGORY_TITLE";

    @DatabaseField(generatedId = true, columnName = CATEGORY_ID_COLOUMN, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField(columnName = CATEGORY_TITLE_COLOUMN)
    private String title;

    @DatabaseField(columnName = CATEGORY_ORDER_FIELD)
    private int orderNumber;

    @DatabaseField(columnName = CATEGORY_VERSION_FIELD)
    private int versionNumber;

    @SerializedName("info")
    private List<Info> infoList;

    public Category(int id){
        this.id = id;
        infoList = new ArrayList<>();
    }

    public Category(){
        infoList = new ArrayList<>();
    }

    // Parcel methods
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
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
        out.writeInt(orderNumber);
        out.writeInt(versionNumber);
        out.writeList(infoList);
    }

    private Category(Parcel in) {
        setId(in.readInt());
        setTitle(in.readString());
        setOrderNumber(in.readInt());
        setVersionNumber(in.readInt());
        infoList = new ArrayList<>();
        in.readList(infoList, Info.class.getClassLoader());
        for(Info info : infoList){
            info.setCategory(this);
        }
    }
}
