package no.rustelefonen.hap.persistence;

import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import no.rustelefonen.hap.entities.Trigger;
import no.rustelefonen.hap.persistence.dao.TriggerDao;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by martinnikolaisorlie on 10/05/16.
 */
@RunWith(AndroidJUnit4.class)
public class TriggerCrudTest{

    private static DatabaseHelper dbHelper;
    private static TriggerDao triggerDao;


    @BeforeClass
    public static void setUpBefore() throws Exception{
        dbHelper = new DatabaseHelper(InstrumentationRegistry.getTargetContext());
        triggerDao = new TriggerDao(dbHelper);
        Log.i("setUp", "passed");
    }

    @AfterClass
    public static void tearDownAfter() throws Exception{
        assertTrue("", dbHelper.isOpen());
        assertTrue(InstrumentationRegistry.getTargetContext().deleteDatabase(DatabaseHelper.DATABASE_NAME));
        dbHelper.close();
        Log.e("testCreateDb", "passed");
    }

    @Test
    public void testCreateTrigger() throws Exception{
        Trigger trigger = new Trigger();
        trigger.setTitle("Test trigger");
        trigger.setColor(Color.RED);
        trigger.setImageId(android.R.drawable.ic_menu_report_image);

        triggerDao.persist(trigger);
        assertTrue("Should now have a id", trigger.getId() > 0);
    }

    @Test
    public void testFetchTrigger() throws Exception{
        Trigger trigger = triggerDao.getById(1);
        assertTrue("Should be Mett", trigger.getTitle().equalsIgnoreCase("mett"));
    }

    @Test
    public void testFetchAll() throws Exception{
        List<Trigger> triggers = triggerDao.getAll();
        assertTrue("Should contain elements", triggers.size() > 0);
    }
}
