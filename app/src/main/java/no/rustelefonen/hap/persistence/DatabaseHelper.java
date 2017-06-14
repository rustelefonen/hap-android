package no.rustelefonen.hap.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import no.rustelefonen.hap.R;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "hap.db";
    private static final int DATABASE_VERSION = 150;
    private boolean shouldUpgrade;
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        this.context = context;
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        handleOnCreateAndOnUpgrade(true);
        return super.getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        handleOnCreateAndOnUpgrade(false);
        return super.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        /*try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, Achievement.class);
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, Info.class);
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, UserTrigger.class);
            TableUtils.createTable(connectionSource, Trigger.class);

            db.beginTransaction();
            InitialLoader.seedThisDB(context, this);
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        shouldUpgrade = true;
        /*if (oldVersion < 150){
            db.execSQL("ALTER TABLE 'UserInfo' ADD COLUMN 'UserType' VARCHAR");
        }*/
        /*try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, Achievement.class, true);
            TableUtils.dropTable(connectionSource, Category.class, true);
            TableUtils.dropTable(connectionSource, Info.class, true);
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, UserTrigger.class, true);
            TableUtils.dropTable(connectionSource, Trigger.class, true);

            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }*/
    }

    private void handleOnCreateAndOnUpgrade(boolean writeable){
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        boolean dbExists = dbFile.exists();

        if(writeable)super.getWritableDatabase().close(); //force create the db file to get write permission at path, and flip the shouldUpgrade bool if needed
        else super.getReadableDatabase().close();

        if(!dbExists || shouldUpgrade) copyPrePopulatedDbTo(dbFile.getAbsolutePath());
    }

    private void copyPrePopulatedDbTo(String dbPath){
        try{
            InputStream assestDB = context.getAssets().open("databases/"+DATABASE_NAME);
            OutputStream appDB = new FileOutputStream(dbPath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = assestDB.read(buffer)) > 0) {
                appDB.write(buffer, 0, length);
            }

            shouldUpgrade = false;
            appDB.flush();
            appDB.close();
            assestDB.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}