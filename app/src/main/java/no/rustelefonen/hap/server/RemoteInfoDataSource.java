package no.rustelefonen.hap.server;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import no.rustelefonen.hap.entities.Category;
import no.rustelefonen.hap.main.tabs.info.InfoTab;
import no.rustelefonen.hap.persistence.OrmLiteActivity;
import no.rustelefonen.hap.persistence.dao.InfoDao;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * Created by martinnikolaisorlie on 28/04/16.
 */
public class RemoteInfoDataSource {
    public static String SERVER_URL = "http://app.rustelefonen.no/";

    private InfoSource remoteService;
    private InfoDao infoDao;
    private final List<Category> localCategories;

    private AtomicInteger requestsCompleted;

    public RemoteInfoDataSource(OrmLiteActivity ormLiteActivity) {
        infoDao = new InfoDao(ormLiteActivity);
        localCategories = infoDao.getAllInfoCategories(false);

        remoteService = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(InfoSource.class);
    }

    /**
     * Checks remote db for version numbers and downloads and updates the outdated categories
     */
    public void syncDatabase() {
        Call<List<CategoryVersion>> remoteCall = remoteService.getCategoryVersions();

        remoteCall.enqueue(new Callback<List<CategoryVersion>>() {
            @Override
            public void onResponse(Call<List<CategoryVersion>> call, Response<List<CategoryVersion>> response) {
                List<CategoryVersion> categoryVersions = response.body();
                if(categoryVersions == null) return;

                deleteCategoriesNotPresentOnRemote(categoryVersions);
                List<CategoryVersion> categoriesToUpdate = collectCategoriesToUpdate(categoryVersions);
                updateOutdatedCategories(categoriesToUpdate);
            }

            @Override
            public void onFailure(Call<List<CategoryVersion>> call, Throwable t) {
                Log.e("Failed to fetch", "Category versions");
            }
        });
    }

    private void deleteCategoriesNotPresentOnRemote(List<CategoryVersion> remoteCategories) {
        for (Category localCategory : localCategories) {
            boolean isOnServer = remoteContainsCategory(localCategory, remoteCategories);
            if(!isOnServer) infoDao.delete(localCategory);
        }
    }

    private List<CategoryVersion> collectCategoriesToUpdate(List<CategoryVersion> remoteCategories) {
        List<CategoryVersion> categoriesToUpdate = new ArrayList<>();

        for (CategoryVersion remoteCategory : remoteCategories) {
            Category localCategoryInTarget = null;
            for (Category localCategory : localCategories) {
                if (localCategory.getId() == remoteCategory.getId()) {
                    localCategoryInTarget = localCategory;
                    break;
                }
            }
            if (localCategoryInTarget == null || localCategoryInTarget.getVersionNumber() < remoteCategory.getVersionNumber()) {
                categoriesToUpdate.add(remoteCategory);
            }
        }

        return categoriesToUpdate;
    }

    private void updateOutdatedCategories(final List<CategoryVersion> categoriesToUpdate) {
        requestsCompleted = new AtomicInteger();

        for (final CategoryVersion category : categoriesToUpdate) {
            Call<Category> remoteCall = remoteService.getCategoryById(category.getId());

            remoteCall.enqueue(new Callback<Category>() {
                @Override
                public void onResponse(Call<Category> call, Response<Category> response) {
                    infoDao.delete(infoDao.getCategoryById(response.body().getId(), true));
                    infoDao.persist(response.body());

                    if (requestsCompleted.incrementAndGet() == categoriesToUpdate.size()) { //if last request, notify info tab to update
                        EventBus.getDefault().post(new InfoTab.UpdateHelpInfoListEvent());
                    }
                }

                @Override
                public void onFailure(Call<Category> call, Throwable t) {
                    Log.e("Failed to fetch", "Categories");
                }
            });
        }
    }

    private boolean remoteContainsCategory(Category category, List<CategoryVersion> remoteCategories){
        boolean isOnServer = false;
        for (CategoryVersion remoteCategory : remoteCategories) {
            if(remoteCategory.getId() == category.getId()){
                isOnServer = true;
                break;
            }
        }
        return isOnServer;
    }

    public interface InfoSource {
        @Headers("Accept: application/json")
        @GET("/api/info/version")
        Call<List<CategoryVersion>> getCategoryVersions();

        @Headers("Accept: application/json")
        @GET("/api/info/categories/{id}")
        Call<Category> getCategoryById(@Path("id") int id);
    }

    @Data
    public class CategoryVersion {
        private Integer id;
        private Integer versionNumber;
    }
}