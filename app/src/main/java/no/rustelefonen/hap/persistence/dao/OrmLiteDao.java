package no.rustelefonen.hap.persistence.dao;

import no.rustelefonen.hap.persistence.DatabaseHelper;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

/**
 * Created by Fredrik on 18.02.2016.
 */
public abstract class OrmLiteDao {

    protected DatabaseHelper dbHelper;

    protected OrmLiteDao(OrmLiteActivity activity) {
        dbHelper = activity.getDatabaseHelper();
    }

    protected OrmLiteDao(DatabaseHelper dbHelper){
        this.dbHelper = dbHelper;
    }
}
