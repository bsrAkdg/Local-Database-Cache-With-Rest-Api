package com.bsrakdg.foodrecipes.requests;

import static com.bsrakdg.foodrecipes.util.Constants.CONNECTION_TIMEOUT;
import static com.bsrakdg.foodrecipes.util.Constants.READ_TIMEOUT;
import static com.bsrakdg.foodrecipes.util.Constants.WRITE_TIMEOUT;

import com.bsrakdg.foodrecipes.util.Constants;
import com.bsrakdg.foodrecipes.util.LiveDataCallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            // establish connection server
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            // time between each byte read from the server
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            // time between each byte sent to server
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false).build();

    private static Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(okHttpClient)
                    .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = retrofitBuilder.build();

    private static RecipeApi recipeApi = retrofit.create(RecipeApi.class);

    public static RecipeApi getRecipeApi(){
        return recipeApi;
    }
}
