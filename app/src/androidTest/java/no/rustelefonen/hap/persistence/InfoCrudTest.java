package no.rustelefonen.hap.persistence;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import no.rustelefonen.hap.entities.Category;
import no.rustelefonen.hap.entities.Info;
import no.rustelefonen.hap.persistence.dao.InfoDao;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;

/**
 * Created by martinnikolaisorlie on 11/05/16.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InfoCrudTest {

    private static DatabaseHelper dbHelper;
    private static InfoDao infoDao;
    private static Context context;

    @BeforeClass
    public static void setUpBefore() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        dbHelper = new DatabaseHelper(context);
        infoDao = new InfoDao(dbHelper);

        Log.i("setUp", "passed");
    }

    @AfterClass
    public static void tearDownAfter() throws Exception {
        assertTrue(dbHelper.isOpen());
        assertTrue(context.deleteDatabase(DatabaseHelper.DATABASE_NAME));
        dbHelper.close();
        Log.e("CreateDb", "passed");
    }


    @Test
    public void stage1_SearchHelpInfo() throws Exception {
        List<Info> list = infoDao.searchInfoTitles("tips");
        assertTrue("Should contain search matching criteria", list.size() > 0);
    }


//    @Test
//    public void stage2_FetchCategoriesWithoutInfoPages() throws Exception {
//        List<Category> categoryList = infoDao.getAllInfoCategories(false);
//        assertTrue("Should now contain elements", categoryList.size() > 0);
//    }

    @Test
    public void stage3_FetchCategoriesWithInfoPages() throws Exception {
        List<Category> categoryList = infoDao.getAllInfoCategories();
        List<List<Info>> infos = new ArrayList<>();
        for (Category c : categoryList) {
            // Log.e("Category", c.getTitle());
            infos.add(c.getInfoList());

        }
       /* for(List<Info> inf : infos){
            for(Info i: inf){
                Log.e("Helpinfo", i.getTitle());
            }
        }*/
        assertTrue("Categories exist", categoryList.size() > 0);
        assertTrue("HelpPages exist under a category", categoryList.get(0).getInfoList().size() > 0);
    }

    @Test
    public void stage4_GetAllHelpInfoPages() throws Exception {
        List<Info> infoPageList = infoDao.getAllInfos();
        assertTrue("Should contain elements", infoPageList.size() > 0);
    }

    @Test
    public void stage5_GetCategoryCount() throws Exception {
        assertTrue("There should exist helpinfocategories", infoDao.getAllInfoCategories().size() > 0);
    }

    @Test
    public void stage6_GetCategoryById() throws Exception {
        Category helpCategory = infoDao.getCategoryById(151, true);
        Log.e("id", "" + helpCategory.getId());
        assertTrue("Should match", helpCategory.getId() == 151);
    }

    @Test
    public void stage7_GetCategoryByName() throws Exception {
        Category helpCategory = infoDao.getCategoryByTitle("hjernen");
        assertTrue("Should match", "hjernen".equalsIgnoreCase(helpCategory.getTitle()));
    }

    @Test
    public void stage9_GetHelpInfoPageByName() throws Exception {
        Info helpInfo = infoDao.getInfoByTitle("3d-hjernen");
        assertTrue("Should match", "3d-hjernen".equalsIgnoreCase(helpInfo.getTitle()));
    }

    @Test
    public void stage1x_CreateInfoPage() throws Exception {
        Info helpInfo = new Info();
        helpInfo.setTitle("Test Infopage");
        helpInfo.setHtmlContent("");
        helpInfo.setCategory(infoDao.getCategoryById(1));

        infoDao.persist(helpInfo);
        assertTrue("Should now exist", helpInfo.getId() > 0);
    }

    @Test
    public void stage1x_CreateInfoCategory() throws Exception {
        Category helpCategory = new Category();
        helpCategory.setTitle("Test Category");
        helpCategory.setOrderNumber(11);
        helpCategory.setVersionNumber(1);

        infoDao.persist(helpCategory);

        assertTrue("Should now exist", helpCategory.getId() > 0);
    }

    @Test
    public void stage1x_LeaveNoOrphans() throws Exception {
        Category helpCategory = infoDao.getCategoryById(7);
        infoDao.delete(helpCategory);
        assertNull(infoDao.getCategoryById(7));
        boolean isDeleted = false;
        try {
            infoDao.getCategoryById(7, true).getInfoList();
        } catch (NullPointerException e) {
            isDeleted = true;
            assertTrue("Should throw NPE", isDeleted);
        }
    }

    @Test
    public void stage1x_DeleteSingleHelpInfoPage() throws Exception {
        infoDao.delete(infoDao.getInfoById(5));
        boolean isDeleted = false;
        try {
            infoDao.getInfoById(5);
        } catch (NullPointerException e) {
            isDeleted = true;
            assertTrue("Should throw NPE", isDeleted);
        }
    }
}
