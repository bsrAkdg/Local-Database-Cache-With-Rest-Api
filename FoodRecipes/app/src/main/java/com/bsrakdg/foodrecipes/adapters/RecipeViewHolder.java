package com.bsrakdg.foodrecipes.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bsrakdg.foodrecipes.R;
import com.bsrakdg.foodrecipes.models.Recipe;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView title, publisher, socialScore;
    private AppCompatImageView image;
    private OnRecipeListener onRecipeListener;
    private RequestManager requestManager;
    private ViewPreloadSizeProvider viewPreloadSizeProvider;

    public RecipeViewHolder(@NonNull View itemView,
                            OnRecipeListener onRecipeListener,
                            RequestManager requestManager,
                            ViewPreloadSizeProvider viewPreloadSizeProvider) {
        super(itemView);

        this.onRecipeListener = onRecipeListener;
        this.requestManager = requestManager;
        this.viewPreloadSizeProvider = viewPreloadSizeProvider;

        title = itemView.findViewById(R.id.recipe_title);
        publisher = itemView.findViewById(R.id.recipe_publisher);
        socialScore = itemView.findViewById(R.id.recipe_social_score);
        image = itemView.findViewById(R.id.recipe_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onRecipeListener.onRecipeClick(getAdapterPosition());
    }

    void onBind(Recipe recipe) {
        requestManager.load(recipe.getImage_url()).into(image);

        title.setText(recipe.getTitle());
        publisher.setText(recipe.getPublisher());
        socialScore.setText(String.valueOf(Math.round(recipe.getSocial_rank())));

        viewPreloadSizeProvider.setView(image);
    }
}





