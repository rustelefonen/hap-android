package no.rustelefonen.hap.entities;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import no.rustelefonen.hap.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Fredrik on 07/05/16.
 */
public class AchievementTest {

    private Achievement achievement;
    private User user;

    @Before
    public void setUp() throws Exception {
        achievement = new Achievement();
        user = new User();
    }

    @Test
    public void getMilestoneImageId() throws Exception {
        achievement.setType(Achievement.Type.MILESTONE);
        assertEquals(R.drawable.milestone, achievement.getImageId(true));
        assertNotEquals(R.drawable.milestone_white, achievement.getImageId(true));
        assertEquals(R.drawable.milestone_white, achievement.getImageId(false));
    }

    @Test
    public void getMinorMilestoneImageId() throws Exception {
        achievement.setType(Achievement.Type.MINOR_MILESTONE);
        assertEquals(R.drawable.minor_milestone, achievement.getImageId(true));
        assertNotEquals(R.drawable.minor_milestone_white, achievement.getImageId(true));
        assertEquals(R.drawable.minor_milestone_white, achievement.getImageId(false));
    }

    @Test
    public void getFinanceImageId() throws Exception {
        achievement.setType(Achievement.Type.FINANCE);
        assertEquals(R.drawable.finance, achievement.getImageId(true));
        assertNotEquals(R.drawable.finance_white, achievement.getImageId(true));
        assertEquals(R.drawable.finance_white, achievement.getImageId(false));
    }

    @Test
    public void getHealthImageId() throws Exception {
        achievement.setType(Achievement.Type.HEALTH);
        assertEquals(R.drawable.health, achievement.getImageId(true));
        assertNotEquals(R.drawable.health_white, achievement.getImageId(true));
        assertEquals(R.drawable.health_white, achievement.getImageId(false));
    }

    @Test
    public void getMilestoneProgress() throws Exception {
        achievement.setPointsRequired(1000);
        achievement.setType(Achievement.Type.MILESTONE);
        assertEquals(0, achievement.getProgress(null), 0);

        user.setStartDate(new Date(System.currentTimeMillis() - 1000*1000)); //user started 1000 sec ago
        assertEquals(1, achievement.getProgress(user), 0.001);
        assertTrue(achievement.isComplete(user));

        user.setStartDate(new Date(System.currentTimeMillis() - 700*1000)); //user started 700 sec ago
        assertEquals(0.7, achievement.getProgress(user), 0.001);
    }

    @Test
    public void getMilestoneProgressBeforeReset() throws Exception {
        achievement.setPointsRequired(1000);
        achievement.setType(Achievement.Type.MILESTONE);
        user.setStartDate(new Date());

        //should use seconds before reset if progress >= 1
        user.setSecondsLastedBeforeLastReset(1000);
        assertEquals(1, achievement.getProgress(user), 0.001);
        assertTrue(achievement.isComplete(user));

        //should not use seconds before reset if progress < 1
        user.setSecondsLastedBeforeLastReset(500);
        assertEquals(0, achievement.getProgress(user), 0.001);
    }

    @Test
    public void getMinorMilestoneProgress() throws Exception {
        achievement.setPointsRequired(1000);
        achievement.setType(Achievement.Type.MINOR_MILESTONE);
        assertEquals(0, achievement.getProgress(null), 0);

        user.setStartDate(new Date(System.currentTimeMillis() - 1000*1000)); //user started 1000 sec ago
        assertEquals(1, achievement.getProgress(user), 0.001);
        assertTrue(achievement.isComplete(user));

        user.setStartDate(new Date(System.currentTimeMillis() - 700*1000)); //user started 700 sec ago
        assertEquals(0.7, achievement.getProgress(user), 0.001);
    }

    @Test
    public void getMinorMilestoneProgressBeforeReset() throws Exception {
        achievement.setPointsRequired(1000);
        achievement.setType(Achievement.Type.MINOR_MILESTONE);
        user.setStartDate(new Date());

        //should use seconds before reset if progress >= 1
        user.setSecondsLastedBeforeLastReset(1000);
        assertEquals(1, achievement.getProgress(user), 0.001);
        assertTrue(achievement.isComplete(user));

        //should not use seconds before reset if progress < 1
        user.setSecondsLastedBeforeLastReset(500);
        assertEquals(0, achievement.getProgress(user), 0.001);
    }

    @Test
    public void getFinanceProgress() throws Exception {
        achievement.setPointsRequired(1000);
        achievement.setType(Achievement.Type.FINANCE);
        assertEquals(0, achievement.getProgress(null), 0);
        user.setStartDate(new Date(System.currentTimeMillis() - 86400*1000)); //one day since started

        user.setMoneySpentPerDayOnHash(0);
        assertEquals(0, achievement.getProgress(user), 0);

        user.setMoneySpentPerDayOnHash(200);
        assertEquals(0.2, achievement.getProgress(user), 0.001);

        user.setMoneySpentPerDayOnHash(1000);
        assertEquals(1, achievement.getProgress(user), 0.001);
        assertTrue(achievement.isComplete(user));
    }

    @Test
    public void getFinanceProgressBeforeReset() throws Exception {
        achievement.setPointsRequired(1000);
        achievement.setType(Achievement.Type.FINANCE);
        assertEquals(0, achievement.getProgress(null), 0);
        user.setStartDate(new Date());
        user.setMoneySpentPerDayOnHash(1000);

        //should use seconds before reset if progress >= 1
        user.setSecondsLastedBeforeLastReset(86400); //one day
        assertEquals(1, achievement.getProgress(user), 0.001);
        assertTrue(achievement.isComplete(user));

        //should not use seconds before reset if progress < 1
        user.setSecondsLastedBeforeLastReset(500);
        assertEquals(0, achievement.getProgress(user), 0.001);
    }

    @Test
    public void secondsToCompletion() throws Exception {
        achievement.setPointsRequired(1000);
        user.setMoneySpentPerDayOnHash(1000);
        user.setStartDate(new Date());

        achievement.setType(Achievement.Type.FINANCE);
        assertEquals(86400, achievement.secondsToCompletion(user), 0.1);

        achievement.setType(Achievement.Type.MILESTONE);
        assertEquals(1000, achievement.secondsToCompletion(user), 0.1);

        achievement.setType(Achievement.Type.MINOR_MILESTONE);
        assertEquals(1000, achievement.secondsToCompletion(user), 0.1);
    }
}
