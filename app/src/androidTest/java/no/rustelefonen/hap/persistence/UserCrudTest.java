package no.rustelefonen.hap.persistence;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.entities.UserTrigger;
import no.rustelefonen.hap.persistence.dao.TriggerDao;
import no.rustelefonen.hap.persistence.dao.UserDao;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by martinnikolaisorlie on 10/05/16.
 */
@RunWith(AndroidJUnit4.class)
public class UserCrudTest{

    private static DatabaseHelper dbHelper;
    private static UserDao userDao;
    private static TriggerDao triggerDao;

    @BeforeClass
    public static void setUpBefore() throws Exception {
        dbHelper = new DatabaseHelper(InstrumentationRegistry.getTargetContext());
        userDao = new UserDao(dbHelper);
        triggerDao = new TriggerDao(dbHelper);
        User user = new User();
        user.setStartDate(new Date());
        userDao.persist(user);
        Log.i("setUp", "passed");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        assertTrue("", dbHelper.isOpen());
        assertTrue(InstrumentationRegistry.getTargetContext().deleteDatabase(DatabaseHelper.DATABASE_NAME));
        dbHelper.close();
        Log.i("tearDown", "passed");

    }

    @Test
    public void testSaveUser() {
        User user = new User();
        user.setStartDate(new Date());

        userDao.persist(user);
        assertTrue(user.getId() > 0);
    }

    @Test
    public void testInvalidUser() {
        boolean passed = false;
        User user = new User();
        try {
            userDao.persist(user);
            passed = true;
        } catch (RuntimeException e) {
            assertFalse("This test should fail due to constraints", passed);
        }
    }

    @Test
    public void testFetchUser() {
        User user = userDao.getFirst();
        assertTrue(user.getId() == 1);
    }

    @Test
    public void testFetchById() {
        User user = new User();
        user.setStartDate(new Date());
        userDao.persist(user);
        User fetchedUser = userDao.getById(2);
        assertEquals("Should now get correct id", 2, fetchedUser.getId());
    }

    @Test
    public void testDeleteUser() throws Exception{
        userDao.delete(userDao.getById(2));
        assertNull("User should not exist", userDao.getById(2));
    }

    @Test
    public void testSaveTriggers() {
        User user = userDao.getFirst();
        UserTrigger userTrigger = new UserTrigger();
        userTrigger.setCount(1);
        userTrigger.setType(UserTrigger.Type.SMOKED);
        userTrigger.setUser(user);
        userTrigger.setTrigger(triggerDao.getById(1));

        List<UserTrigger> smokedTriggers = new ArrayList<>();
        smokedTriggers.add(userTrigger);
        user.setUnSavedTriggers(smokedTriggers);

        userDao.batchPersistUnsavedTriggers(user);
        assertTrue(userDao.getFirst().getSmokedTriggers().size() == 1);
        assertTrue(userDao.getFirst().getResistedTriggers().isEmpty());
        assertTrue(userDao.getFirst().getUnSavedTriggers().isEmpty());
    }

}
