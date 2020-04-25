package com.bsrakdg.foodrecipes.adapters;

import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bsrakdg.foodrecipes.R;
import com.bsrakdg.foodrecipes.models.Recipe;
import com.bumptech.glide.RequestManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private CircleImageView categoryImage;
    private TextView categoryTitle;
    private OnRecipeListener listener;
    private RequestManager requestManager;

    CategoryViewHolder(@NonNull View itemView, OnRecipeListener listener,
                       RequestManager requestManager) {
        super(itemView);

        this.listener = listener;
        this.requestManager = requestManager;
        categoryImage = itemView.findViewById(R.id.category_image);
        categoryTitle = itemView.findViewById(R.id.category_title);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        listener.onCategoryClick(categoryTitle.getText().toString());
    }

    void onBind(Recipe recipe) {
        Uri path = Uri.parse("android.resource://com.bsrakdg.foodrecipes/drawable/" + recipe
                .getImage_url());
        requestManager.load(path).into(categoryImage);
        categoryTitle.setText(recipe.getTitle());
    }
}
