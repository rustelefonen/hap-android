package no.rustelefonen.hap.persistence;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

import no.rustelefonen.hap.entities.Achievement;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.entities.Category;
import no.rustelefonen.hap.entities.Trigger;
import no.rustelefonen.hap.entities.User;
import no.rustelefonen.hap.entities.UserTrigger;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class ORMConfigMaker extends OrmLiteConfigUtil {
    private static final Class[] classes = new Class[]{ Achievement.class,
            Category.class, Info.class, User.class, UserTrigger.class, Trigger.class};

    public static void main(String[] args) throws SQLException, IOException {
        writeConfigFile("ormlite_config.txt", classes);
    }
}