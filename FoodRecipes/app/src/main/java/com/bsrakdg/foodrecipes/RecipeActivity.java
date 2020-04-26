package com.bsrakdg.foodrecipes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bsrakdg.foodrecipes.models.Recipe;
import com.bsrakdg.foodrecipes.util.Resource;
import com.bsrakdg.foodrecipes.viewmodels.RecipeViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class RecipeActivity extends BaseActivity {

    private static final String TAG = "RecipeActivity";

    // UI components
    private AppCompatImageView mRecipeImage;
    private TextView mRecipeTitle, mRecipeRank;
    private LinearLayout mRecipeIngredientsContainer;
    private ScrollView mScrollView;

    private RecipeViewModel mRecipeViewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        mRecipeImage = findViewById(R.id.recipe_image);
        mRecipeTitle = findViewById(R.id.recipe_title);
        mRecipeRank = findViewById(R.id.recipe_social_score);
        mRecipeIngredientsContainer = findViewById(R.id.ingredients_container);
        mScrollView = findViewById(R.id.parent);

        mRecipeViewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);

        getIncomingIntent();
    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra("recipe")){
            Recipe recipe = getIntent().getParcelableExtra("recipe");
            Log.d(TAG, "getIncomingIntent: " + recipe.getTitle());
            subscribeObservers(recipe.getRecipe_id());
        }
    }

    private void subscribeObservers(final String recipeId) {
        mRecipeViewModel.searchRecipeApi(recipeId).observe(this, new Observer<Resource<Recipe>>() {
            @Override
            public void onChanged(Resource<Recipe> recipeResource) {
                if (recipeResource != null && recipeResource.data != null) {
                    switch (recipeResource.status) {
                        case LOADING:
                            showProgressBar(true);
                            break;
                        case ERROR:
                            Log.d(TAG, "onChanged: sstatus : ERROR, Recipe : " + recipeResource.data
                                    .getTitle());
                            Log.d(TAG, "onChanged: ERROR message " + recipeResource.message);
                            setRecipeProperties(recipeResource.data);
                            showParent();
                            showProgressBar(false);
                            break;
                        case SUCCESS:
                            Log.d(TAG, "onChanged: chache has been refreshed");
                            Log.d(TAG,
                                    "onChanged: status : SUCCESS, Recipe : " + recipeResource.data
                                            .toString());
                            setRecipeProperties(recipeResource.data);
                            showParent();
                            showProgressBar(false);
                            break;

                        default:
                            break;
                    }
                }
            }
        });
    }

    private void setIngredients(Recipe recipe) {
        mRecipeIngredientsContainer.removeAllViews();
        if (recipe.getIngredients() != null) {
            for (String ingredient : recipe.getIngredients()) {
                TextView textView = new TextView(this);
                textView.setText(ingredient);
                textView.setTextSize(15);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                mRecipeIngredientsContainer.addView(textView);
            }
        } else {
            TextView textView = new TextView(this);
            textView.setText("Error retrieving ingredients.\nCheck network connection");
            textView.setTextSize(15);
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            mRecipeIngredientsContainer.addView(textView);
        }
    }

    private void setRecipeProperties(Recipe recipe) {
        if (recipe != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.white_background)
                    .error(R.drawable.white_background);
            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(recipe.getImage_url())
                    .into(mRecipeImage);

            mRecipeTitle.setText(recipe.getTitle());
            mRecipeRank.setText(String.valueOf(Math.round(recipe.getSocial_rank())));

            setIngredients(recipe);
        }
    }

    private void showParent(){
        mScrollView.setVisibility(View.VISIBLE);
    }
}














