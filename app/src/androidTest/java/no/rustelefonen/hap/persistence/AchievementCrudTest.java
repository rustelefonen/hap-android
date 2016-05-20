package no.rustelefonen.hap.persistence;

import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.persistence.dao.AchievementDao;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;


/**
 * Created by martinnikolaisorlie on 10/05/16.
 */
@RunWith(AndroidJUnit4.class)
public class AchievementCrudTest{

    private static DatabaseHelper dbHelper;
    private static AchievementDao achievementDao;
    private static User user;
    private static SQLiteDatabase db;

    @BeforeClass
    public static void setUpBefore() throws Exception{
        dbHelper = new DatabaseHelper(InstrumentationRegistry.getTargetContext());
        db = dbHelper.getWritableDatabase();
        achievementDao = new AchievementDao(dbHelper);
        user = new User();
        Log.e("testDropDb", "passed");
    }

    @AfterClass
    public static void tearDownAfter() throws Exception{
        assertTrue("", db.isOpen());
        assertTrue(InstrumentationRegistry.getTargetContext().deleteDatabase(DatabaseHelper.DATABASE_NAME));
        db.close();
        dbHelper.close();
        Log.e("testCreateDb", "passed");
    }


    @Test
    public void testInvalidInsert(){
        boolean passed = false;
        Achievement achievement = new Achievement();
        try {
            achievementDao.persist(achievement);
            passed = true;
        }catch (RuntimeException e){
            assertFalse("This test should fail", passed);
        }
    }

    @Test
    public void testFetchAll(){
        List<Achievement> list = achievementDao.fetchAll();
        assertTrue("List should contain some elements", list.size() > 0);
    }

    @Test
    public void testFetchById(){
        Achievement achievement = achievementDao.fetchById(1);
        assertEquals("Should be the first", "Første dagen uten!", achievement.getTitle());
    }

    @Test
    public void testFetchUpcoming()throws Exception{
        user.setStartDate(new Date());
        user.setId(1);

        Achievement achievement = achievementDao.fetchNextUpcoming(user);
        assertTrue(achievement.getId() > 0);
    }

    @Test
    public void testFetchAllUpcoming() throws Exception{
        user.setStartDate(new Date());
        user.setId(1);

        List<Achievement> list = achievementDao.fetchAllUpcomingSorted(user);
        assertTrue("Should contain elements", list.size() > 0);
    }

    @Test
    public void testCompletedAchievements() throws Exception{
        user.setStartDate(new Date());
        user.setId(1);

        List<Achievement> list = achievementDao.fetchCompletedSorted(user);
        assertTrue("Should contain elements", list.size() > 0);
        assertEquals("First element should contain", "Første skrittet!", list.get(0).getTitle());
    }

    @Test
    public void testFetchUpcomingAndCompleted() throws Exception{
        user.setStartDate(new Date());
        user.setId(1);

        List<Achievement> list = achievementDao.fetchNextUpcomingAndAllCompletedSorted(user);
        assertTrue("Should not be empty", list.size() > 0);

    }

    @Test
    public void testInsertAchievementData(){

        Achievement achievement = new Achievement();
        achievement.setTitle("TestAchievement");
        achievement.setDescription("Test Description");
        achievement.setPointsRequired(10);
        achievement.setType(Achievement.Type.MILESTONE);

        assertNotNull(achievementDao.persist(achievement));
        assertTrue(achievement.getId() > 0);
        Log.e("test", "passed");
    }

}
