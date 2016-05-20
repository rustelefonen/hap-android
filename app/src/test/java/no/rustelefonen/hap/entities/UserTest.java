package no.rustelefonen.hap.entities;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by Fredrik on 26/02/16.
 */
public class UserTest {

    private User user;

    @Before
    public void setUp(){
        user = new User();
        user.setStartDate(new Date(System.currentTimeMillis() - 86400*1000)); //one day ago
        user.setMoneySpentPerDayOnHash(200);
    }

    @Test
    public void secondsSinceStarted() throws Exception {
        assertEquals(86400, user.secondsSinceStarted(), 0.01);
    }

    @Test
    public void totalMoneySaved() throws Exception {
        assertEquals(200, user.totalMoneySaved(), 0.0001);
    }

    @Test
    public void totalMoneySavedBeforeReset() throws Exception {
        user.setSecondsLastedBeforeLastReset(20000);
        assertEquals(46.29, user.totalMoneySavedBeforeReset(), 0.01);
    }

    @Test
    public void moneySavedPerSecond() throws Exception {
        assertEquals(0.0023, user.moneySavedPerSecond(), 0.0001);
    }

    @Test
    public void daysSinceStarted() throws Exception {
        user.setStartDate(new Date(System.currentTimeMillis() - 86400*1000*7));
        assertEquals(7, user.daysSinceStarted());
    }

    @Test
    public void addTrigger() throws Exception {
        assertEquals(0, user.getResistedTriggers().size());
        Trigger trigger = new Trigger();
        trigger.setId(1);

        user.addTrigger(trigger, UserTrigger.Type.RESISTED);
        assertEquals(1, user.getResistedTriggers().size());
        assertEquals(1, user.getResistedTriggers().get(0).getCount());

        user.addTrigger(trigger, UserTrigger.Type.RESISTED);
        assertEquals(1, user.getResistedTriggers().size()); //size not increased
        assertEquals(2, user.getResistedTriggers().get(0).getCount()); //but count did

        user.addTrigger(new Trigger(), UserTrigger.Type.RESISTED);
        assertEquals(2, user.getResistedTriggers().size()); //count increased because trigger was new

        user.addTrigger(trigger, UserTrigger.Type.SMOKED);
        assertEquals(1, user.getSmokedTriggers().size());
    }
}
