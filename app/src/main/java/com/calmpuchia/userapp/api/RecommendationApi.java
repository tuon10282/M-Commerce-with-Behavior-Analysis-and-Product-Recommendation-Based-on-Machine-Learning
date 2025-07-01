package com.calmpuchia.userapp.api;

import com.calmpuchia.userapp.models.RecommendationResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RecommendationApi {
    @GET("api/recommendations/{userId}")
    Call<RecommendationResponse> getRecommendations(@Path("userId") String userId);
}
