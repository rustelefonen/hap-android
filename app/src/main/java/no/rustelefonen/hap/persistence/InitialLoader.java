package no.rustelefonen.hap.persistence;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

import no.rustelefonen.hap.R;
import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.entities.Category;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.entities.Trigger;
import no.rustelefonen.hap.persistence.dao.AchievementDao;
import no.rustelefonen.hap.persistence.dao.InfoDao;
import no.rustelefonen.hap.persistence.dao.TriggerDao;

/**
 * Created by martinnikolaisorlie on 03/02/16.
 */
public class InitialLoader {

    public static void seedThisDB(Context context, DatabaseHelper dbHelper) throws SQLException {
        AchievementDao achievementDao = new AchievementDao(dbHelper);
        InfoDao infoDao = new InfoDao(dbHelper);
        TriggerDao triggerDao = new TriggerDao(dbHelper);

        Resources res = context.getResources();

        try {
            for(String s : res.getStringArray(R.array.helpinfo)){
                JSONObject json = new JSONObject(s);
                Info info = new Info();
                info.setTitle(json.getString("title"));
                info.setHtmlContent(json.getString("url"));

                Category category = new Category();
                category.setId(json.getInt("categoryId"));
                info.setCategory(category);

                infoDao.persist(info);
            }

            for(String s : res.getStringArray(R.array.helpinfo_category)){
                JSONObject json = new JSONObject(s);
                Category category = new Category();
                category.setTitle(json.getString("title"));
                category.setOrderNumber(json.getInt("orderNumber"));
                category.setVersionNumber(json.getInt("versionNumber"));
                infoDao.persist(category);
            }

            for(String s : res.getStringArray(R.array.achievements)){
                JSONObject json = new JSONObject(s);
                Achievement achievement = new Achievement();
                achievement.setTitle(json.getString("title"));
                achievement.setDescription(json.getString("description"));
                achievement.setPointsRequired(json.getInt("pointsRequired"));
                achievement.setType(Achievement.Type.valueOf(json.getString("type").toUpperCase()));
                achievementDao.persist(achievement);
            }

            for(String s : res.getStringArray(R.array.triggers)){
                JSONObject json = new JSONObject(s);
                Trigger trigger = new Trigger();
                trigger.setTitle(json.getString("title"));
                trigger.setImageId(res.getIdentifier(json.getString("image"), "drawable", context.getPackageName()));
                trigger.setColor(Color.parseColor(json.getString("color")));
                triggerDao.persist(trigger);
            }

        } catch (JSONException ignored) {
            ignored.printStackTrace();
        }
    }
}
