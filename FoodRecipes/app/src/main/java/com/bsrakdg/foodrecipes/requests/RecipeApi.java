package com.bsrakdg.foodrecipes.requests;

import androidx.lifecycle.LiveData;

import com.bsrakdg.foodrecipes.requests.responses.ApiResponse;
import com.bsrakdg.foodrecipes.requests.responses.RecipeResponse;
import com.bsrakdg.foodrecipes.requests.responses.RecipeSearchResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeApi {

    // GET RECIPE REQUEST
    @GET("api/get")
    LiveData<ApiResponse<RecipeResponse>> getRecipe(
            @Query("rId") String recipe_id
    );

    // SEARCH
    @GET("api/search")
    LiveData<ApiResponse<RecipeSearchResponse>> searchRecipe(
            @Query("q") String query,
            @Query("page") String page
    );
}
