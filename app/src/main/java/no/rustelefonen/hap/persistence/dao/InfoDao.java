package no.rustelefonen.hap.persistence.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import no.rustelefonen.hap.entities.Category;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.persistence.DatabaseHelper;
import no.rustelefonen.hap.persistence.OrmLiteActivity;

/**
 * Created by martinnikolaisorlie on 27/01/16.
 */
public class InfoDao extends OrmLiteDao {

    private Dao<Info, Integer> helpInfoDao;
    private Dao<Category, Integer> categoryDao;

    public InfoDao(OrmLiteActivity activity) {
        super(activity);
        initDao();
    }

    public InfoDao(DatabaseHelper dbHelper) {
        super(dbHelper);
        initDao();
    }

    private void initDao() {
        try {
            helpInfoDao = dbHelper.getDao(Info.class);
            categoryDao = dbHelper.getDao(Category.class);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param info the entity to be persisted
     * @return the same entity as passed by param, id should now be set
     * @throws RuntimeException in case of SQLException
     */
    public Info persist(Info info) {
        if (info == null) return null;

        try {
            helpInfoDao.createOrUpdate(info);
            return info;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param category the entity to be persisted
     * @return the same entity as passed by param, id should now be set
     * @throws RuntimeException in case of SQLException
     */
    public Category persist(Category category) {
        if (category == null) return null;

        try {
            categoryDao.createOrUpdate(category);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        for (Info info : category.getInfoList()) {
            info.setCategory(category);
            persist(info);
        }
        return category;
    }

    /**
     * @param info the entity to be deleted
     * @throws RuntimeException in case of SQLException
     */
    public void delete(Info info) {
        if (info == null) return;

        try {
            helpInfoDao.delete(info);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param category the entity to be deleted
     * @throws RuntimeException in case of SQLException
     */
    public void delete(Category category) {
        if (category == null) return;

        for (Info info : category.getInfoList()) {
            delete(info);
        }

        try {
            categoryDao.delete(category);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param id the info entity id
     * @return the info entity associated with the passed id, or null if not found
     * @throws RuntimeException in case of SQLException
     */
    public Info getInfoById(int id) {
        try {
            Info info = helpInfoDao.queryForId(id);
            if (info == null) return null;

            Category category = categoryDao.queryForId(info.getCategory().getId());
            info.setCategory(category);
            return info;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param infoTitle the info entity title
     * @return the info entity associated with the passed title, or null if not found
     * @throws RuntimeException in case of SQLException
     */
    public Info getInfoByTitle(String infoTitle) {
        try {
            return helpInfoDao.queryBuilder()
                    .where()
                    .like(Info.INFO_TITLE_COLOUMN, infoTitle)
                    .queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Same as calling  {@link #searchInfoTitles(String, boolean true)}
     * @return List of Info entities with title containing value
     * @throws RuntimeException in case of SQLException
     */
    public List<Info> searchInfoTitles(String value){
        return searchInfoTitles(value, true);
    }

    /**
     * @param value title search string
     * @param excludeHtmlContent whether or not to fetch the whole htmlContant string of the entities
     * @return List of Info entities with title containing value
     * @throws RuntimeException in case of SQLException
     */
    public List<Info> searchInfoTitles(String value, boolean excludeHtmlContent) {
        try {
            QueryBuilder<Info, Integer> builder = helpInfoDao.queryBuilder();
            if(excludeHtmlContent) builder = builder.selectColumns(Info.INFO_TITLE_COLOUMN, Info.INFO_CATEGORY_ID);

            return builder.where()
                    .like(Info.INFO_TITLE_COLOUMN, "%"+value+"%")
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Same as calling {@link #getCategoryByTitle(String, boolean true)}
     * @param categoryTitle the category entity title
     * @return the category entity associated with the passed title, or null if not found
     * @throws RuntimeException in case of SQLException
     */
    public Category getCategoryByTitle(String categoryTitle){
        return getCategoryByTitle(categoryTitle, true);
    }

    /**
     * @param categoryTitle the category entity title
     * @return the category entity associated with the passed title, or null if not found
     * @throws RuntimeException in case of SQLException
     */
    public Category getCategoryByTitle(String categoryTitle, boolean excludeHtmlContent) {
        try {
            Category category = categoryDao.queryBuilder()
                    .where()
                    .like(Category.CATEGORY_TITLE_COLOUMN, categoryTitle)
                    .queryForFirst();
            if (category == null) return null;

            category.setInfoList(fetchInfoForCategory(category, excludeHtmlContent));
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Same as calling {@link #getCategoryById(int, boolean true)}
     * @param id the category entity id
     * @return the category entity associated with the passed id, or null if not found
     * @throws RuntimeException in case of SQLException
     */
    public Category getCategoryById(int id){
        return getCategoryById(id, true);
    }

    /**
     * @param id the category entity id
     * @param excludeHtmlContent whether or not to fetch the whole htmlContant string of the entities
     * @return the category entity associated with the passed id, or null if not found
     * @throws RuntimeException in case of SQLException
     */
    public Category getCategoryById(int id, boolean excludeHtmlContent) {
        try {
            Category category = categoryDao.queryForId(id);
            if (category == null) return null;

            category.setInfoList(fetchInfoForCategory(category, excludeHtmlContent));
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @return List of all info entities in the db
     * @throws RuntimeException in case of SQLException
     */
    public List<Info> getAllInfos() {
        try {
            List<Info> infoList = helpInfoDao.queryForAll();
            for (Info info : infoList) {
                if (info.getCategory() != null) {
                    Category category = categoryDao.queryForId(info.getCategory().getId());
                    info.setCategory(category);
                }
            }
            return infoList;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @return List of all category entities in the db, excluding their htmlContent
     * @throws RuntimeException in case of SQLException
     */
    public List<Category> getAllInfoCategories() {
        return getAllInfoCategories(true);
    }

    /**
     * @param excludeSubInfosHtmlContent whether or not to fetch the info entities html content
     * @return List of all category entities in the db
     * @throws RuntimeException in case of SQLException
     */
    public List<Category> getAllInfoCategories(boolean excludeSubInfosHtmlContent) {
        try {
            List<Category> categories =  categoryDao.queryBuilder()
                    .orderBy(Category.CATEGORY_ORDER_FIELD, true)
                    .query();

            for(Category category : categories){
                category.setInfoList(fetchInfoForCategory(category, excludeSubInfosHtmlContent));
            }

            return categories;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param category The entity for which to fetch associated info entities
     * @param excludeHtmlContent whether or not to exclude the htmlContent string of the info entities
     * @return List of info entities associated the the passed category
     * @throws SQLException
     */
    private List<Info> fetchInfoForCategory(Category category, boolean excludeHtmlContent) throws SQLException {
        QueryBuilder<Info, Integer> builder = helpInfoDao.queryBuilder();
        if(excludeHtmlContent) builder = builder.selectColumns(Info.INFO_TITLE_COLOUMN, Info.INFO_CATEGORY_ID);

        return builder.orderBy(Info.INFO_TITLE_COLOUMN, true)
                .where()
                .eq(Info.INFO_CATEGORY_ID, category.getId())
                .query();
    }
}