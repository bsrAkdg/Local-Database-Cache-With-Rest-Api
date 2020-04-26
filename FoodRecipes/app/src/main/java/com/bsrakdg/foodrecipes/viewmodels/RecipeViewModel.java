package com.bsrakdg.foodrecipes.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.bsrakdg.foodrecipes.models.Recipe;
import com.bsrakdg.foodrecipes.repositories.RecipeRepository;
import com.bsrakdg.foodrecipes.util.Resource;

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository recipeRepository;

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = RecipeRepository.getInstance(application);
    }

    public LiveData<Resource<Recipe>> searchRecipeApi(String recipeId) {
        return recipeRepository.searchRecipeApi(recipeId);
    }
}





















