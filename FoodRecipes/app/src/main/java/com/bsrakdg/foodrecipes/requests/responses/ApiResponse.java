package com.bsrakdg.foodrecipes.requests.responses;

import java.io.IOException;

import retrofit2.Response;

public class ApiResponse<T> {
    // There is three case for network layer
    // A request response can be success, error or empty

    public ApiResponse<T> create(Throwable error) {
        return new ApiErrorResponse<>(!error.getMessage().equals("") ? error
                .getMessage() : "Unknow error\nCheck network connection");
    }

    public ApiResponse<T> create(Response<T> response) {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body == null || response.code() == 204) { // 204 no content error
                return new ApiEmptyResponse<>();
            } else {
                return new ApiSuccessResponse<>(body);
            }
        } else {
            String errorMsg = "";
            try {
                errorMsg = response.errorBody().string();
            } catch (IOException exception) {
                exception.printStackTrace();
                errorMsg = response.message();
            }
            return new ApiErrorResponse<>(errorMsg);
        }
    }

    public class ApiSuccessResponse<T> extends ApiResponse<T> {
        private T body;

        public ApiSuccessResponse(T body) {
            this.body = body;
        }

        public T getBody() {
            return body;
        }
    }

    public class ApiErrorResponse<T> extends ApiResponse<T> {
        private String errorMessage;

        public ApiErrorResponse(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public class ApiEmptyResponse<T> extends ApiResponse<T> {
    }
}
