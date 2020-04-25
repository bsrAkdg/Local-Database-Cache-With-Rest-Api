package com.bsrakdg.foodrecipes.viewmodels;

import android.app.Application;
import android.util.Log;

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
    public static final String QUERY_EXHAUSTED = "No more results";

    private MutableLiveData<ViewState> viewStateMutableLiveData;
    private MediatorLiveData<Resource<List<Recipe>>> recipes = new MediatorLiveData<>();
    private RecipeRepository recipeRepository;

    // query extras
    private boolean isQueryExhausted;
    private boolean isPerformingQuery;
    private int pageNumber;
    private String query;

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
        if (!isPerformingQuery) {
            if (pageNumber == 0) {
                pageNumber = 1;
            }
            this.pageNumber = pageNumber;
            this.query = query;
            isQueryExhausted = false;
            executeSearch();
        }
    }

    public MutableLiveData<ViewState> getViewStateMutableLiveData() {
        return viewStateMutableLiveData;
    }

    private void executeSearch() {
        isPerformingQuery = true;
        viewStateMutableLiveData.setValue(ViewState.RECIPES);

        final LiveData<Resource<List<Recipe>>> repositorySource = recipeRepository.searchRecipesApi(query, pageNumber);
        recipes.addSource(repositorySource, new Observer<Resource<List<Recipe>>>() {
            @Override
            public void onChanged(Resource<List<Recipe>> listResource) {
                if (listResource != null) {
                    // react to the data
                    recipes.setValue(listResource);
                    if (listResource.status == Resource.Status.SUCCESS) {
                        isPerformingQuery = false;
                        if (listResource.data != null) {
                            if (listResource.data.size() == 0) {
                                Log.d(TAG, "onChanged: query is exhausted...");
                                recipes.setValue(new Resource<>(
                                        Resource.Status.ERROR,
                                        listResource.data,
                                        QUERY_EXHAUSTED
                                ));
                            }
                        }
                        recipes.removeSource(repositorySource);
                    } else if (listResource.status == Resource.Status.ERROR) {
                        isPerformingQuery = false;
                        recipes.removeSource(repositorySource);
                    }
                } else {
                    recipes.removeSource(repositorySource);
                }
            }
        });
    }
    public int getPageNumber() {
        return pageNumber;
    }

    public enum ViewState {CATEGORIES, RECIPES}

}















