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

    public Info persist(Info info) {
        if (info == null) return null;

        try {
            helpInfoDao.createOrUpdate(info);
            return info;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

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

    public void delete(Info info) {
        if (info == null) return;

        try {
            helpInfoDao.delete(info);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

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

    public Info getInfoByName(String helpInfoName) {
        try {
            return helpInfoDao.queryBuilder()
                    .where()
                    .like(Info.INFO_TITLE_COLOUMN, helpInfoName)
                    .queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Info> searchInfoTitles(String value){
        return searchInfoTitles(value, true);
    }

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

    public Category getCategoryByName(String categoryName){
        return getCategoryByName(categoryName, true);
    }

    public Category getCategoryByName(String categoryName, boolean excludeHtmlContent) {
        try {
            Category category = categoryDao.queryBuilder()
                    .where()
                    .like(Category.CATEGORY_TITLE_COLOUMN, categoryName)
                    .queryForFirst();
            if (category == null) return null;

            category.setInfoList(fetchInfoForCategory(category, excludeHtmlContent));
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Category getCategoryById(int id){
        return getCategoryById(id, true);
    }

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

    public List<Category> getAllInfoCategories(boolean fetchSubInfos) {
        try {
            List<Category> categories =  categoryDao.queryBuilder()
                    .orderBy(Category.CATEGORY_ORDER_FIELD, true)
                    .query();

            if(fetchSubInfos){
                for(Category category : categories){
                    category.setInfoList(fetchInfoForCategory(category, true));
                }
            }

            return categories;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<Info> fetchInfoForCategory(Category category, boolean excludeHtmlContent) {
        try {
            QueryBuilder<Info, Integer> builder = helpInfoDao.queryBuilder();
            if(excludeHtmlContent) builder = builder.selectColumns(Info.INFO_TITLE_COLOUMN, Info.INFO_CATEGORY_ID);


            return builder.orderBy(Info.INFO_TITLE_COLOUMN, true)
                    .where()
                    .eq(Info.INFO_CATEGORY_ID, category.getId())
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}