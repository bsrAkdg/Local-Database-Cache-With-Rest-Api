package com.bsrakdg.foodrecipes.repositories;

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
                return false;
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
}
