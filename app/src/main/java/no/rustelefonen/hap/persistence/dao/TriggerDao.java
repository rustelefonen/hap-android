package no.rustelefonen.hap.persistence.dao;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import no.rustelefonen.hap.entities.Trigger;
import no.rustelefonen.hap.persistence.DatabaseHelper;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

/**
 * Created by Fredrik on 27.02.2016.
 */
public class TriggerDao extends OrmLiteDao {

    private Dao<Trigger, Integer> triggerDao;

    public TriggerDao(OrmLiteActivity activity) {
        super(activity);
        initDao();
    }

    public TriggerDao(DatabaseHelper dbHelper) {
        super(dbHelper);
        initDao();
    }

    private void initDao() {
        try {
            triggerDao = dbHelper.getDao(Trigger.class);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Trigger persist(Trigger trigger) {
        if(trigger == null) return null;

        try {
            triggerDao.createOrUpdate(trigger);
            return trigger;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Trigger getById(int id) {
        try {
            return triggerDao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Trigger> getAll() {
        try {
            return triggerDao.queryBuilder().orderBy(Trigger.TRIGGER_TITLE_COLOUMN, true).query();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
