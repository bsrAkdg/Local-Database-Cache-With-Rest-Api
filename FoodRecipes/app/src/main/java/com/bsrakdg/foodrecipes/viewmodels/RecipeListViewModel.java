package com.bsrakdg.foodrecipes.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.bsrakdg.foodrecipes.models.Recipe;
import com.bsrakdg.foodrecipes.repositories.RecipeRepository;
import com.bsrakdg.foodrecipes.util.Resource;

import java.util.List;

public class RecipeListViewModel extends AndroidViewModel {

    private static final String TAG = "RecipeListViewModel";

    private MutableLiveData<ViewState> viewStateMutableLiveData;
    private MediatorLiveData<Resource<List<Recipe>>> recipes = new MediatorLiveData<>();
    private RecipeRepository recipeRepository;

    public RecipeListViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = RecipeRepository.getInstance(application);
        init();
    }

    private void init() {
        if (viewStateMutableLiveData == null) {
            viewStateMutableLiveData = new MutableLiveData<>();
            viewStateMutableLiveData.setValue(ViewState.CATEGORIES);
        }
    }

    public LiveData<Resource<List<Recipe>>> getRecipes() {
        return recipes;
    }

    public void searchRecipesApi(String query, int pageNumber) {
        final LiveData<Resource<List<Recipe>>> repositorySource = recipeRepository.searchRecipesApi(query, pageNumber);
        recipes.addSource(repositorySource, new Observer<Resource<List<Recipe>>>() {
            @Override
            public void onChanged(Resource<List<Recipe>> listResource) {
                // react to the data
                recipes.setValue(listResource);
            }
        });
    }
    public MutableLiveData<ViewState> getViewStateMutableLiveData() {
        return viewStateMutableLiveData;
    }

    public enum ViewState {CATEGORIES, RECIPES}

}















