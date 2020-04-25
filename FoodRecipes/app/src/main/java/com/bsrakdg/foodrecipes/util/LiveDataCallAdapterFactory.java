package com.bsrakdg.foodrecipes.util;

import androidx.lifecycle.LiveData;

import com.bsrakdg.foodrecipes.requests.responses.ApiResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public class LiveDataCallAdapterFactory extends CallAdapter.Factory {
    /**
     * This method performs a number of checks and then returns the Response type for the
     * Retrofit request.
     * (@bodyType is the ResponseType. It can be RecipeResponse or RecipeSearchResponse)
     * <p>
     * CHECK #1) returnType return LiveData
     * CHECK #2) Type LiveData<T> is of ApiResponse.class
     * CHECK #2) Make sure ApiResponse is parameterized. AKA: ApiResponse<T> exists.
     */

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        // Check #1
        // Make sure the CallAdapter is returning a type of LiveData
        if (CallAdapter.Factory.getRawType(returnType) != LiveData.class) {
            return null;
        }

        // Check #2
        // Type that LiveData is wrapping
        Type observableType = CallAdapter.Factory
                .getParameterUpperBound(0, (ParameterizedType) returnType);

        //Check if it's of Type ApiResponse
        Type rawObservableType = CallAdapter.Factory.getRawType(observableType);
        if (rawObservableType != ApiResponse.class) {
            throw new IllegalArgumentException("Type must be a defined resource");
        }

        // Check #3
        // Check if ApiResponse is parameterized. AKA: Does ApiResponse<T> exist? (must around T)
        // FYI : T is either RecipeResponse or T will be a RecipeSearchResponse
        if (!(observableType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Resource must be parameterized");
        }

        Type bodyType = CallAdapter.Factory
                .getParameterUpperBound(0, (ParameterizedType) observableType);
        return new LiveDataCallAdapter<Type>(bodyType);
    }
}
