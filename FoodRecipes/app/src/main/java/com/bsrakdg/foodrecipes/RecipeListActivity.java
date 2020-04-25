package com.bsrakdg.foodrecipes;

import static com.bsrakdg.foodrecipes.viewmodels.RecipeListViewModel.QUERY_EXHAUSTED;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;


import com.bsrakdg.foodrecipes.adapters.OnRecipeListener;
import com.bsrakdg.foodrecipes.adapters.RecipeRecyclerAdapter;
import com.bsrakdg.foodrecipes.models.Recipe;
import com.bsrakdg.foodrecipes.util.Resource;
import com.bsrakdg.foodrecipes.util.VerticalSpacingItemDecorator;
import com.bsrakdg.foodrecipes.viewmodels.RecipeListViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;


public class RecipeListActivity extends BaseActivity implements OnRecipeListener {

    private static final String TAG = "RecipeListActivity";

    private RecipeListViewModel mRecipeListViewModel;
    private RecyclerView mRecyclerView;
    private RecipeRecyclerAdapter mAdapter;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        mRecyclerView = findViewById(R.id.recipe_list);
        mSearchView = findViewById(R.id.search_view);

        mRecipeListViewModel = ViewModelProviders.of(this).get(RecipeListViewModel.class);
        subscribeObservers();

        initRecyclerView();
        initSearchView();

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

    }

    private void subscribeObservers() {
        mRecipeListViewModel.getRecipes().observe(this, new Observer<Resource<List<Recipe>>>() {
            @Override
            public void onChanged(Resource<List<Recipe>> listResource) {
                if (listResource != null) {
                    Log.d(TAG, "onChanged: status " + listResource.status);
                    if (listResource.data != null) {
                        // Testing.printRecipes(listResource.data, TAG + "+ data");
                        switch (listResource.status) {
                            case LOADING:
                                if (mRecipeListViewModel.getPageNumber() > 1) {
                                    mAdapter.displayLoading();
                                } else {
                                    // click category after :
                                    mAdapter.displayOnlyLoading();
                                }
                                break;
                            case ERROR:
                                Log.e(TAG, "onChanged: cannot refresh the cache");
                                Log.e(TAG, "onChanged: Error message " + listResource.message);
                                Log.e(TAG, "onChanged: status ERROR, #recipes " + listResource.data.size());
                                mAdapter.hideLoading();
                                mAdapter.setRecipes(listResource.data);
                                Toast.makeText(RecipeListActivity.this, listResource.message, Toast.LENGTH_LONG).show();

                                if (listResource.message.equals(QUERY_EXHAUSTED)) {
                                    mAdapter.setQueryExhausted();
                                }
                                break;
                            case SUCCESS:
                                Log.d(TAG, "onChanged: Cache has been refreshed");
                                Log.d(TAG, "onChanged: status SUCCESS, #recipes " + listResource.data.size());
                                mAdapter.setRecipes(listResource.data);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });
        mRecipeListViewModel.getViewStateMutableLiveData().observe(this, new Observer<RecipeListViewModel.ViewState>() {
            @Override
            public void onChanged(@Nullable RecipeListViewModel.ViewState viewState) {
                if (viewState != null) {
                    if (viewState == RecipeListViewModel.ViewState.CATEGORIES) {
                        displaySearchCategories();
                    } else if (viewState == RecipeListViewModel.ViewState.RECIPES) {
                        // recipes will show automatically from another observer
                    }
                }
            }
        });
    }

    private void searchRecipesApi(String query) {
        mRecipeListViewModel.searchRecipesApi(query, 1);
    }

    private void displaySearchCategories() {
        mAdapter.displaySearchCategories();
    }

    private void initRecyclerView(){
        mAdapter = new RecipeRecyclerAdapter(this, initGlide());
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(30);
        mRecyclerView.addItemDecoration(itemDecorator);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initSearchView(){
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchRecipesApi(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onRecipeClick(int position) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe", mAdapter.getSelectedRecipe(position));
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(String category) {
        searchRecipesApi(category);
    }

    private RequestManager initGlide() {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.white_background)
                .error(R.drawable.white_background);
        return  Glide.with(this).setDefaultRequestOptions(requestOptions);
    }
}

















