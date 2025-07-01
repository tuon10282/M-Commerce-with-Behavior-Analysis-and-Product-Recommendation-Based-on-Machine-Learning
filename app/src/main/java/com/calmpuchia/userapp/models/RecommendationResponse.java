package com.calmpuchia.userapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecommendationResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("model_info")
    private ModelInfo modelInfo;

    @SerializedName("recommendations")
    private List<Recommendation> recommendations;

    @SerializedName("response_time_ms")
    private double responseTimeMs;

    @SerializedName("user_id")
    private String userId;

    // Getters
    public int getCount() {
        return count;
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public double getResponseTimeMs() {
        return responseTimeMs;
    }

    public String getUserId() {
        return userId;
    }

    // ModelInfo nested class
    public static class ModelInfo {
        @SerializedName("last_trained")
        private double lastTrained;

        @SerializedName("matrix_shape")
        private int[] matrixShape;

        @SerializedName("n_factors")
        private int nFactors;

        @SerializedName("n_products")
        private int nProducts;

        @SerializedName("n_users")
        private int nUsers;

        @SerializedName("status")
        private String status;

        // Getters
        public double getLastTrained() { return lastTrained; }
        public int[] getMatrixShape() { return matrixShape; }
        public int getNFactors() { return nFactors; }
        public int getNProducts() { return nProducts; }
        public int getNUsers() { return nUsers; }
        public String getStatus() { return status; }
    }

    // Recommendation nested class
    public static class Recommendation {
        @SerializedName("product_id")
        private String productId;

        @SerializedName("product_info")
        private ProductsRec productInfo;

        @SerializedName("score")
        private double score;

        // Getters
        public String getProductId() { return productId; }
        public ProductsRec getProductInfo() { return productInfo; }
        public double getScore() { return score; }
    }
}