package com.bsrakdg.foodrecipes.util;

import android.util.Log;

import com.bsrakdg.foodrecipes.models.Recipe;

import java.util.List;

public class Testing {

    public static void printRecipes(List<Recipe>list, String tag){
        for(Recipe recipe: list){
            Log.d(tag, "onChanged data : " + recipe.getTitle());
        }
    }
}
