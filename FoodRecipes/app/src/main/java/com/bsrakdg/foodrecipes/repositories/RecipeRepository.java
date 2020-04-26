package com.bsrakdg.foodrecipes.repositories;

import static com.bsrakdg.foodrecipes.util.Constants.RECIPE_REFRESH_TIME;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.bsrakdg.foodrecipes.AppExecutors;
import com.bsrakdg.foodrecipes.models.Recipe;
import com.bsrakdg.foodrecipes.persistence.RecipeDao;
import com.bsrakdg.foodrecipes.persistence.RecipeDatabase;
import com.bsrakdg.foodrecipes.requests.ServiceGenerator;
import com.bsrakdg.foodrecipes.requests.responses.ApiResponse;
import com.bsrakdg.foodrecipes.requests.responses.RecipeResponse;
import com.bsrakdg.foodrecipes.requests.responses.RecipeSearchResponse;
import com.bsrakdg.foodrecipes.util.NetworkBoundResource;
import com.bsrakdg.foodrecipes.util.Resource;

import java.util.List;

public class RecipeRepository {

    private static final String TAG = "RecipeRepository";

    private static  RecipeRepository instance;
    private RecipeDao recipeDao;

    private RecipeRepository(Context context) {
        this.recipeDao = RecipeDatabase.getInstance(context).getRecipeDao();
    }

    public static RecipeRepository getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeRepository(context);
        }
        return instance;
    }

    public LiveData<Resource<List<Recipe>>> searchRecipesApi(final  String query, final int pageNumber) {
        return new NetworkBoundResource<List<Recipe>, RecipeSearchResponse>(AppExecutors.getInstance()){

            @Override
            protected void saveCallResult(@NonNull RecipeSearchResponse item) {
                if (item.getRecipes() != null) { // will be null
                    Recipe[] recipes = new Recipe[item.getRecipes().size()];
                    int index = 0;
                    for (long rowId : recipeDao.insertRecipe((item.getRecipes().toArray(recipes)))) {
                        if (rowId == -1) { // there is conflict
                            Log.d(TAG, "saveCallResult: CONFLICT .. This recipe is already in the cache");
                            // if the recipe already exists... I don't want to set the ingredients or timestamp
                            // they will be erased
                            recipeDao.updateRecipe(recipes[index].getRecipe_id(),
                                    recipes[index].getTitle(),
                                    recipes[index].getPublisher(),
                                    recipes[index].getImage_url(),
                                    recipes[index].getSocial_rank());
                        }
                        index++;
                    }
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Recipe> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Recipe>> loadFromDb() {
                return recipeDao.searchRecipes(query, pageNumber);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<RecipeSearchResponse>> createCall() {
                return ServiceGenerator.getRecipeApi()
                        .searchRecipe(query, String.valueOf(pageNumber));
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<Recipe>> searchRecipeApi(final String recipeId) {
        return new NetworkBoundResource<Recipe, RecipeResponse>(AppExecutors.getInstance()) {

            @Override
            protected void saveCallResult(@NonNull RecipeResponse item) {
                if (item.getRecipe() != null) {
                    item.getRecipe().setTimestamp((int) System.currentTimeMillis() / 1000);
                    recipeDao.insertRecipe(item.getRecipe());
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable Recipe data) {
                Log.d(TAG, "shouldFetch: recipe " + data.toString());
                int currentTime = (int)(System.currentTimeMillis() / 1000);
                Log.d(TAG, "shouldFetch: current time " + currentTime);
                int lastRefresh = data.getTimestamp();
                Log.d(TAG, "shouldFetch: last refresh " + lastRefresh);
                Log.d(TAG, "shouldFetch: it's been "
                        + ((currentTime - lastRefresh) / 60 / 60 / 24)
                        + " days since this recipe was refreshed. 30 days must elapse before refreshing");
                if ((currentTime - lastRefresh) >= RECIPE_REFRESH_TIME) {
                    Log.d(TAG, "shouldFetch: SHOULD REFRESH RECIPE " + true);
                    return true;
                }
                Log.d(TAG, "shouldFetch: SHOULD REFRESH RECIPE " + false);
                return false;
            }

            @NonNull
            @Override
            protected LiveData<Recipe> loadFromDb() {
                return recipeDao.getRecipe(recipeId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<RecipeResponse>> createCall() {
                return ServiceGenerator.getRecipeApi().getRecipe(recipeId);
            }
        }.getAsLiveData();
    }
}
