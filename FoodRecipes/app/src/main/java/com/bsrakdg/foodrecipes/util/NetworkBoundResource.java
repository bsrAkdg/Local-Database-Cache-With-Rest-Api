package com.bsrakdg.foodrecipes.util;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.bsrakdg.foodrecipes.AppExecutors;
import com.bsrakdg.foodrecipes.requests.responses.ApiResponse;

// Look at : https://developer.android.com/jetpack/docs/guide#addendum
// CacheObject: Type for the Resource data. (database cache) : ResultType
// RequestObject: Type for the API response. (network request) : RequestType
public abstract class NetworkBoundResource<CacheObject, RequestObject> {

    private static final String TAG = "NetworkBoundResource";

    private MediatorLiveData<Resource<CacheObject>>  results = new MediatorLiveData<>();
    private AppExecutors appExecutors; // some methods work background thread (example @WorkerThread)

    public NetworkBoundResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        init();
    }

    /**
     * 1) observe local db
     * 2) if <condition/> query the network
     * 3) stop observing the local db
     * 4) insert new data into local db
     * 5) begin observing local db again to see the refreshed data from network
     *
     * @param dbSource
     */
    private void fetchFromNetwork(final LiveData<CacheObject> dbSource) {
        Log.d(TAG, "fetchFromNetwork: called");

        // update livedata for loading status
        results.addSource(dbSource, new Observer<CacheObject>() {
            @Override
            public void onChanged(@Nullable CacheObject cacheObject) {
                setValue(Resource.loading(cacheObject));
            }
        });

        final LiveData<ApiResponse<RequestObject>> apiResponse = createCall();

        results.addSource(apiResponse, new Observer<ApiResponse<RequestObject>>() {
            @Override
            public void onChanged(@Nullable final ApiResponse<RequestObject> requestObjectApiResponse) {
                results.removeSource(dbSource);
                results.removeSource(apiResponse);

                /*  3 cases :
                        1) ApiSuccessResponse
                        2) ApiErrorResponse
                        3) ApiEmptyResponse
                 */

                if (requestObjectApiResponse instanceof ApiResponse.ApiSuccessResponse) {
                    Log.d(TAG, "onChanged: ApiSuccessResponse");

                    appExecutors.getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            // 4. save the response to the local db
                            saveCallResult((RequestObject) processResponse((ApiResponse.ApiSuccessResponse) requestObjectApiResponse));

                            // 5. begin observing local db again to see the refreshed data from
                            // network
                            appExecutors.getMainThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    results.addSource(loadFromDb(), new Observer<CacheObject>() {
                                        @Override
                                        public void onChanged(@Nullable CacheObject cacheObject) {
                                            setValue(Resource.success(cacheObject));
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else if (requestObjectApiResponse instanceof ApiResponse.ApiEmptyResponse) {
                    Log.d(TAG, "onChanged: ApiEmptyResponse");
                    appExecutors.getMainThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            results.addSource(loadFromDb(), new Observer<CacheObject>() {
                                @Override
                                public void onChanged(@Nullable CacheObject cacheObject) {
                                    setValue(Resource.success(cacheObject));
                                }
                            });
                        }
                    });
                } else if (requestObjectApiResponse instanceof ApiResponse.ApiErrorResponse) {
                    Log.d(TAG, "onChanged: ApiErrorResponse");
                    results.addSource(dbSource, new Observer<CacheObject>() {
                        @Override
                        public void onChanged(@Nullable CacheObject cacheObject) {
                            setValue(Resource
                                    .error(((ApiResponse.ApiErrorResponse) requestObjectApiResponse)
                                            .getErrorMessage(), cacheObject));
                        }
                    });
                }
            }
        });

    }

    private void init() {
        //update LiveData for loading status
        results.setValue((Resource<CacheObject>) Resource.loading(null));

        // 1. observe local db
        //observe LiveData source from local db
        final LiveData<CacheObject> dbSource = loadFromDb();

        results.addSource(dbSource, new Observer<CacheObject>() {
            @Override
            public void onChanged(@Nullable CacheObject cacheObject) {
                // 3. stop observing the local db
                results.removeSource(dbSource);
                // 2. if <condition/> query the network
                if (shouldFetch(cacheObject)) {
                    // get data from the network
                    fetchFromNetwork(dbSource);
                } else {
                    // get data from the cache
                    results.addSource(dbSource, new Observer<CacheObject>() {
                        @Override
                        public void onChanged(@Nullable CacheObject cacheObject) {
                            setValue(Resource.success(cacheObject));
                        }
                    });
                }
            }
        });
    }

    private CacheObject processResponse(ApiResponse.ApiSuccessResponse response) {
        return (CacheObject) response.getBody();
    }
    private void setValue(Resource<CacheObject> newValue) {
        if (results.getValue() != newValue) {
            results.setValue(newValue);
        }
    }
    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestObject item);

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    protected abstract boolean shouldFetch(@Nullable CacheObject data);

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<CacheObject> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestObject>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<CacheObject>> getAsLiveData(){
        return results;
    }
}