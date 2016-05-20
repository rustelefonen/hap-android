package no.rustelefonen.hap.persistence.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.persistence.DatabaseHelper;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

/**
 * Created by Fredrik Loberg on 27/01/16.
 */
public class AchievementDao extends OrmLiteDao {

    private Dao<Achievement, Integer> achievementDao;

    public AchievementDao(OrmLiteActivity activity) {
        super(activity);
        initDao();
    }

    public AchievementDao(DatabaseHelper dbHelper) {
        super(dbHelper);
        initDao();
    }

    private void initDao() {
        try {
            achievementDao = dbHelper.getDao(Achievement.class);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Achievement persist(Achievement achievement) {
        if(achievement == null) return null;

        try {
            achievementDao.createOrUpdate(achievement);
            return achievement;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Achievement fetchById(int id) {
        try {
            return achievementDao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Achievement> fetchAll() {
        try {
            return achievementDao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Achievement> fetchCompletedSorted(User user) {
        if(user == null) return null;

        List<Achievement> nextUpcomingAndAllCompleted = fetchNextUpcomingAndAllCompletedSorted(user);

        if(nextUpcomingAndAllCompleted.size() >= 2){
            return nextUpcomingAndAllCompleted.subList(1, nextUpcomingAndAllCompleted.size()); //removing next upcoming
        }
        return new ArrayList<>();
    }

    public List<Achievement> fetchNextUpcomingAndAllCompletedSorted(User user) {
        if(user == null) return null;

        List<Achievement> allAchievements = fetchAllTimeBasedSorted(user);
        List<Achievement> achievements = new ArrayList<>();
        for(Achievement achievement : allAchievements){
            achievements.add(achievement);
            if(!achievement.isComplete(user)) break;
        }

        Collections.reverse(achievements);
        return achievements;
    }

    public Achievement fetchNextUpcoming(User user) {
        if(user == null) return null;
        List<Achievement> allUpcoming = fetchAllUpcomingSorted(user);
        return allUpcoming.size() > 0 ? allUpcoming.get(0) : null;
    }

    public List<Achievement> fetchAllUpcomingSorted(User user) {
        if(user == null) return null;
        List<Achievement> achievements = fetchAllTimeBasedSorted(user);

        for(int i = 0; i < achievements.size(); i++){
            if(achievements.get(i).secondsToCompletion(user) > 0){
                return achievements.subList(i, achievements.size()); //list is sorted
            }
        }
        return new ArrayList<>();
    }

    public Achievement fetchNextUpcomingOfTypes(List<Achievement.Type> types, User user) {
        if(user == null) return null;
        List<Achievement> achievements = fetchAllUpcomingSorted(user);

        for(Achievement achievement : achievements){
            if(types.contains(achievement.getType())){
                return achievement;
            }
        }
        return null;
    }

    private List<Achievement> fetchAllTimeBasedSorted(final User user) {
        try{
            Where<Achievement, Integer> where = achievementDao.queryBuilder()
                    .where().eq(Achievement.ACHIEVEMENT_TYPE_COLOUMN, Achievement.Type.MILESTONE)
                    .or()
                    .eq(Achievement.ACHIEVEMENT_TYPE_COLOUMN, Achievement.Type.MINOR_MILESTONE)
                    .or()
                    .eq(Achievement.ACHIEVEMENT_TYPE_COLOUMN, Achievement.Type.HEALTH);

            if(user.moneySavedPerSecond() >= 0){
                where = where.or().eq(Achievement.ACHIEVEMENT_TYPE_COLOUMN, Achievement.Type.FINANCE);
            }

            List<Achievement> allAchievements = where.query();
            Collections.sort(allAchievements, new Comparator<Achievement>() {
                @Override
                public int compare(Achievement l, Achievement r) {
                    double leftValue = l.secondsToCompletion(user);
                    double rightValue = r.secondsToCompletion(user);

                    return leftValue < rightValue ? -1
                            : leftValue > rightValue ? 1
                            : 0;
                }
            });
            return allAchievements;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void delete(Achievement achievement){
        try{
            achievementDao.delete(achievement);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
