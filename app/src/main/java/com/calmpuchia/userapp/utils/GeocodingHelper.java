package com.calmpuchia.userapp.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeocodingHelper {

    private static final String TAG = "GeocodingHelper";

    // Sử dụng Nominatim API (OpenStreetMap) - hoàn toàn miễn phí
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/search";

    // Backup: LocationIQ API (có free tier)
    private static final String LOCATIONIQ_BASE_URL = "https://us1.locationiq.com/v1/search.php";
    private static final String LOCATIONIQ_API_KEY = "pk.0123456789abcdef"; // Demo key, bạn có thể đăng ký free

    public interface GeocodingCallback {
        void onSuccess(double lat, double lng);
        void onError(String error);
    }

    public static void getLatLngFromAddress(String address, GeocodingCallback callback) {
        Log.d(TAG, "Starting geocoding for address: " + address);

        // Chuẩn hóa địa chỉ trước
        String normalizedAddress = normalizeVietnameseAddress(address);
        Log.d(TAG, "Normalized address: " + normalizedAddress);

        // Thử Nominatim trước (hoàn toàn miễn phí)
        getLatLngFromNominatim(normalizedAddress, new GeocodingCallback() {
            @Override
            public void onSuccess(double lat, double lng) {
                Log.d(TAG, "Nominatim success: " + lat + ", " + lng);
                callback.onSuccess(lat, lng);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Nominatim failed: " + error);
                // Nếu Nominatim thất bại, thử với địa chỉ gốc
                if (!normalizedAddress.equals(address)) {
                    Log.d(TAG, "Retrying with original address");
                    getLatLngFromNominatim(address + ", Vietnam", new GeocodingCallback() {
                        @Override
                        public void onSuccess(double lat, double lng) {
                            Log.d(TAG, "Nominatim success with original: " + lat + ", " + lng);
                            callback.onSuccess(lat, lng);
                        }

                        @Override
                        public void onError(String error2) {
                            Log.w(TAG, "Both Nominatim attempts failed");
                            // Thử với các biến thể khác
                            tryAlternativeSearches(address, callback);
                        }
                    });
                } else {
                    tryAlternativeSearches(address, callback);
                }
            }
        });
    }

    private static void tryAlternativeSearches(String originalAddress, GeocodingCallback callback) {
        Log.d(TAG, "Trying alternative searches for: " + originalAddress);

        // Tạo các biến thể tìm kiếm
        String[] alternatives = createSearchAlternatives(originalAddress);

        tryAlternativeRecursive(alternatives, 0, callback);
    }

    private static void tryAlternativeRecursive(String[] alternatives, int index, GeocodingCallback callback) {
        if (index >= alternatives.length) {
            // Nếu tất cả các biến thể đều thất bại, sử dụng tọa độ mặc định
            Log.w(TAG, "All geocoding attempts failed, using default coordinates");
            getApproximateLocation(alternatives[0], callback);
            return;
        }

        String currentAlternative = alternatives[index];
        Log.d(TAG, "Trying alternative " + (index + 1) + ": " + currentAlternative);

        getLatLngFromNominatim(currentAlternative, new GeocodingCallback() {
            @Override
            public void onSuccess(double lat, double lng) {
                Log.d(TAG, "Alternative " + (index + 1) + " success: " + lat + ", " + lng);
                callback.onSuccess(lat, lng);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Alternative " + (index + 1) + " failed: " + error);
                // Thử biến thể tiếp theo
                tryAlternativeRecursive(alternatives, index + 1, callback);
            }
        });
    }

    private static String[] createSearchAlternatives(String address) {
        String lowerAddress = address.toLowerCase().trim();

        return new String[]{
                // Biến thể 1: Thêm Vietnam
                address + ", Vietnam",

                // Biến thể 2: Thêm TP.HCM nếu chưa có
                lowerAddress.contains("hồ chí minh") || lowerAddress.contains("hcm") || lowerAddress.contains("tp.hcm")
                        ? address + ", Vietnam"
                        : address + ", TP.HCM, Vietnam",

                // Biến thể 3: Thêm Thủ Đức nếu là trường đại học
                (lowerAddress.contains("đại học") && !lowerAddress.contains("thủ đức"))
                        ? address + ", Thủ Đức, TP.HCM, Vietnam"
                        : address + ", Vietnam",

                // Biến thể 4: Chỉ lấy phần chính
                extractMainLocation(address) + ", Vietnam",

                // Biến thể 5: Tìm kiếm chung chung hơn
                lowerAddress.contains("bách khoa") ? "Đại học Bách Khoa TP.HCM, Vietnam" : address + ", Vietnam"
        };
    }

    private static String extractMainLocation(String address) {
        String main = address.toLowerCase().trim();

        // Nếu có "đại học", chỉ lấy phần đại học
        if (main.contains("đại học")) {
            if (main.contains("bách khoa")) return "Đại học Bách Khoa";
            if (main.contains("kinh tế")) return "Đại học Kinh tế - Luật";
            if (main.contains("khoa học tự nhiên")) return "Đại học Khoa học Tự nhiên";
            if (main.contains("công nghệ thông tin")) return "Đại học Công nghệ Thông tin";
            if (main.contains("quốc tế")) return "Đại học Quốc tế";
        }

        return address;
    }

    private static String normalizeVietnameseAddress(String address) {
        String normalized = address.toLowerCase().trim();

        // Chuẩn hóa các từ viết tắt
        normalized = normalized.replaceAll("\\btp\\b", "thành phố");
        normalized = normalized.replaceAll("\\bq\\b", "quận");
        normalized = normalized.replaceAll("\\bp\\b", "phường");
        normalized = normalized.replaceAll("\\btx\\b", "thị xã");
        normalized = normalized.replaceAll("\\btt\\b", "thị trấn");
        normalized = normalized.replaceAll("\\bhcm\\b", "hồ chí minh");

        // Loại bỏ khoảng trắng thừa
        normalized = normalized.replaceAll("\\s+", " ").trim();

        // Thêm Vietnam nếu chưa có
        if (!normalized.contains("vietnam")) {
            normalized += ", vietnam";
        }

        return normalized;
    }

    private static void getLatLngFromNominatim(String address, GeocodingCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String url = NOMINATIM_BASE_URL +
                    "?q=" + encodedAddress +
                    "&format=json" +
                    "&limit=3" + // Tăng limit để có nhiều lựa chọn
                    "&countrycodes=vn" +
                    "&addressdetails=1" +
                    "&extratags=1";

            Log.d(TAG, "Nominatim URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "CalmPuchiaApp/1.0")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Nominatim network error", e);
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError("Network error: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Nominatim response: " + responseBody);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);

                            if (jsonArray.length() > 0) {
                                // Chọn kết quả tốt nhất
                                JSONObject bestResult = findBestResult(jsonArray, address);
                                double lat = bestResult.getDouble("lat");
                                double lng = bestResult.getDouble("lon");

                                Log.d(TAG, "Selected result: " + bestResult.optString("display_name"));
                                callback.onSuccess(lat, lng);
                            } else {
                                callback.onError("Không tìm thấy địa chỉ");
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error", e);
                            callback.onError("Error parsing response: " + e.getMessage());
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Request creation error", e);
            callback.onError("Error creating request: " + e.getMessage());
        }
    }

    private static JSONObject findBestResult(JSONArray results, String originalAddress) throws JSONException {
        String lowerOriginal = originalAddress.toLowerCase();
        JSONObject bestResult = results.getJSONObject(0); // Default to first result
        double bestScore = 0;

        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String displayName = result.optString("display_name", "").toLowerCase();

            double score = calculateMatchScore(lowerOriginal, displayName);
            Log.d(TAG, "Result " + i + " score: " + score + " - " + displayName);

            if (score > bestScore) {
                bestScore = score;
                bestResult = result;
            }
        }

        return bestResult;
    }

    private static double calculateMatchScore(String original, String candidate) {
        double score = 0;

        // Kiểm tra các từ khóa quan trọng
        if (original.contains("bách khoa") && candidate.contains("bách khoa")) score += 10;
        if (original.contains("kinh tế") && candidate.contains("kinh tế")) score += 10;
        if (original.contains("khoa học") && candidate.contains("khoa học")) score += 10;
        if (original.contains("công nghệ") && candidate.contains("công nghệ")) score += 10;
        if (original.contains("quốc tế") && candidate.contains("quốc tế")) score += 10;

        // Kiểm tra vị trí
        if (original.contains("thủ đức") && candidate.contains("thủ đức")) score += 5;
        if (original.contains("hồ chí minh") && candidate.contains("hồ chí minh")) score += 3;
        if (original.contains("linh trung") && candidate.contains("linh trung")) score += 5;

        return score;
    }

    // Phương thức backup với tọa độ chính xác hơn cho các trường đại học
    public static void getApproximateLocation(String address, GeocodingCallback callback) {
        String lowerAddress = address.toLowerCase();
        Log.d(TAG, "Using approximate location for: " + address);

        // Tọa độ chính xác của các trường đại học ở khu vực Thủ Đức
        if (lowerAddress.contains("bách khoa")) {
            // Đại học Bách Khoa TP.HCM - cơ sở Dĩ An
            callback.onSuccess(10.878014, 106.806239);
        } else if (lowerAddress.contains("công nghệ thông tin")) {
            // Đại học Công nghệ Thông tin
            callback.onSuccess(10.87097, 106.80396);
        } else if (lowerAddress.contains("kinh tế") || lowerAddress.contains("luật")) {
            // Đại học Kinh tế - Luật
            callback.onSuccess(10.870157, 106.80213);
        } else if (lowerAddress.contains("khoa học tự nhiên")) {
            // Đại học Khoa học Tự nhiên
            callback.onSuccess(10.872643, 106.799423);
        } else if (lowerAddress.contains("quốc tế")) {
            // Đại học Quốc tế
            callback.onSuccess(10.871472, 106.802627);
        } else if (lowerAddress.contains("thủ đức")) {
            // Trung tâm Thủ Đức
            callback.onSuccess(10.853000, 106.765000);
        } else if (lowerAddress.contains("linh trung")) {
            // Khu vực Linh Trung
            callback.onSuccess(10.870000, 106.802000);
        } else {
            // Mặc định: trung tâm TP.HCM
            callback.onSuccess(10.762622, 106.660172);
        }
    }
}