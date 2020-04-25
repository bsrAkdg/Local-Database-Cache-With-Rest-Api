package com.bsrakdg.foodrecipes.persistence;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.bsrakdg.foodrecipes.models.Recipe;

import java.util.List;

@Dao
public interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE recipe_id = :recipe_id")
    LiveData<Recipe> getRecipe(String recipe_id);

    // don't care return type just replace
    @Insert(onConflict = REPLACE)
    void insertRecipe(Recipe recipe);

    // no conflict return = { id1, id2, id3, ...}
    // no conflict return = { -1, id2, -1, ...} id1 and id3 conflict
    @Insert(onConflict = IGNORE)
    long[] insertRecipe(Recipe... recipes);

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR ingredients LIKE '%' "
            + "|| :query || '%' "
            + "ORDER BY social_rank DESC LIMIT(:pageNumber * 30)")
    LiveData<List<Recipe>> searchRecipes(String query, int pageNumber);

    @Query("UPDATE recipes SET title = :title, publisher = :publisher, image_url = :image_url, "
            + "social_rank= :social_rank WHERE recipe_id = :recipe_id")
    void updateRecipe(String recipe_id, String title, String publisher,
                      String image_url, float social_rank);
}
