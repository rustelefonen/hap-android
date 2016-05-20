package no.rustelefonen.hap.persistence.dao;

import com.google.common.collect.Iterables;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.entities.UserTrigger;
import no.rustelefonen.hap.persistence.DatabaseHelper;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

/**
 * Created by martinnikolaisorlie on 27/01/16.
 */
public class UserDao extends OrmLiteDao {

    private Dao<User, Integer> userDao;
    private Dao<UserTrigger, Integer> userTriggerDao;
    private TriggerDao triggerDao;

    public UserDao(OrmLiteActivity activity) {
        super(activity);
        initDao();
    }

    public UserDao(DatabaseHelper dbHelper) {
        super(dbHelper);
        initDao();
    }

    private void initDao() {
        try {
            userDao = dbHelper.getDao(User.class);
            userTriggerDao = dbHelper.getDao(UserTrigger.class);
            triggerDao = new TriggerDao(dbHelper);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean persist(final User user) {
        if(user == null) return false;
        try {
            userDao.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws SQLException {
                    userDao.createOrUpdate(user);
                    syncUserTriggers(user);
                    return null;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void delete(User user) {
        try {
            userDao.delete(user);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean batchPersistUnsavedTriggers(final User user) {
        try {
            userTriggerDao.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    persistUnsavedTriggers(user);
                    return null;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void persistUnsavedTriggers(User user) {
        if(user == null) return;

        try {
            for(UserTrigger userTrigger : user.getUnSavedTriggers()){
                userTriggerDao.createOrUpdate(userTrigger);
            }
            user.setUnSavedTriggers(new ArrayList<UserTrigger>());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public User getById(int id) {
        try {
            User user = userDao.queryForId(id);
            if(user == null) return null;

            fetchAndAttachTriggers(user);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public User getFirst() {
        try {
            User user = userDao.queryBuilder().queryForFirst();
            if(user == null) return null;

            fetchAndAttachTriggers(user);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void fetchAndAttachTriggers(User user) {
        List<UserTrigger> resistedTriggers = getUserTriggers(user, UserTrigger.Type.RESISTED);
        List<UserTrigger> smokedTriggers = getUserTriggers(user, UserTrigger.Type.SMOKED);

        //because it contains dummy objects by default
        for(UserTrigger userTrigger : Iterables.concat(resistedTriggers, smokedTriggers)){
            userTrigger.setUser(user);
            userTrigger.setTrigger(triggerDao.getById(userTrigger.getTrigger().getId()));
        }

        user.setResistedTriggers(resistedTriggers);
        user.setSmokedTriggers(smokedTriggers);
    }


    private List<UserTrigger> getUserTriggers(User user, UserTrigger.Type type) {
        try {
            return userTriggerDao.queryBuilder()
                    .where()
                    .eq(UserTrigger.USER_FK_COLOUMN, user.getId())
                    .and()
                    .eq(UserTrigger.TRIGGER_TYPE_COLOUMN, type)
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void syncUserTriggers(User user) {
        List<UserTrigger> resisted = user.getResistedTriggers();
        List<UserTrigger> smoked = user.getSmokedTriggers();

        try {
            //removing deleted triggers from data store
            for(UserTrigger userTrigger : Iterables.concat(getUserTriggers(user, UserTrigger.Type.RESISTED), getUserTriggers(user, UserTrigger.Type.SMOKED))){
                if(resisted.contains(userTrigger) || smoked.contains(userTrigger)) continue;
                userTriggerDao.delete(userTrigger);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        persistUnsavedTriggers(user);
    }
}