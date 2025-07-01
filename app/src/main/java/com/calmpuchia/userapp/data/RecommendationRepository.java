package com.calmpuchia.userapp.data;

import com.calmpuchia.userapp.api.RecommendationApi;
import com.calmpuchia.userapp.models.RecommendationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecommendationRepository {

    private static final String BASE_URL = "https://5eb4b433-85bf-44e7-b2e5-325704c89bf8-00-2285gfvmit1f6.spock.replit.dev/";
    private final RecommendationApi api;

    public RecommendationRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(RecommendationApi.class);
    }

    public void getRecommendations(String userId, Callback<RecommendationResponse> callback) {
        Call<RecommendationResponse> call = api.getRecommendations(userId);
        call.enqueue(callback);
    }
}