package com.bsrakdg.foodrecipes.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

public class RecipeListViewModel extends AndroidViewModel {

    private static final String TAG = "RecipeListViewModel";

    private MutableLiveData<ViewState> viewStateMutableLiveData;

    public RecipeListViewModel(@NonNull Application application) {
        super(application);
        init();
    }

    private void init() {
        if (viewStateMutableLiveData == null) {
            viewStateMutableLiveData = new MutableLiveData<>();
            viewStateMutableLiveData.setValue(ViewState.CATEGORIES);
        }
    }

    public MutableLiveData<ViewState> getViewStateMutableLiveData() {
        return viewStateMutableLiveData;
    }

    public enum ViewState {CATEGORIES, RECIPES}

}















