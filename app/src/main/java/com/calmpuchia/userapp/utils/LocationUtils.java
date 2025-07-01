package com.calmpuchia.userapp.utils;

public class LocationUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Tính khoảng cách giữa hai điểm trên trái đất bằng công thức Haversine
     * @param lat1 Vĩ độ điểm 1
     * @param lng1 Kinh độ điểm 1
     * @param lat2 Vĩ độ điểm 2
     * @param lng2 Kinh độ điểm 2
     * @return Khoảng cách tính bằng km
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Chuyển đổi từ độ sang radian
        double lat1Rad = Math.toRadians(lat1);
        double lng1Rad = Math.toRadians(lng1);
        double lat2Rad = Math.toRadians(lat2);
        double lng2Rad = Math.toRadians(lng2);

        // Tính hiệu số
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLng = lng2Rad - lng1Rad;

        // Công thức Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Khoảng cách tính bằng km
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Kiểm tra xem một điểm có nằm trong bán kính cho trước không
     * @param centerLat Vĩ độ tâm
     * @param centerLng Kinh độ tâm
     * @param pointLat Vĩ độ điểm cần kiểm tra
     * @param pointLng Kinh độ điểm cần kiểm tra
     * @param radiusKm Bán kính tính bằng km
     * @return true nếu điểm nằm trong bán kính
     */
    public static boolean isWithinRadius(double centerLat, double centerLng,
                                         double pointLat, double pointLng,
                                         double radiusKm) {
        double distance = calculateDistance(centerLat, centerLng, pointLat, pointLng);
        return distance <= radiusKm;
    }

    /**
     * Format khoảng cách để hiển thị
     * @param distanceKm Khoảng cách tính bằng km
     * @return Chuỗi hiển thị khoảng cách
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceKm * 1000);
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }

    /**
     * Tính bounding box để tối ưu hóa query database
     * @param centerLat Vĩ độ tâm
     * @param centerLng Kinh độ tâm
     * @param radiusKm Bán kính tính bằng km
     * @return Mảng double [minLat, maxLat, minLng, maxLng]
     */
    public static double[] getBoundingBox(double centerLat, double centerLng, double radiusKm) {
        // Tính toán độ thay đổi lat/lng tương ứng với radiusKm
        double latChange = radiusKm / 111.0; // 1 độ lat ≈ 111km
        double lngChange = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));

        double minLat = centerLat - latChange;
        double maxLat = centerLat + latChange;
        double minLng = centerLng - lngChange;
        double maxLng = centerLng + lngChange;

        return new double[]{minLat, maxLat, minLng, maxLng};
    }
}