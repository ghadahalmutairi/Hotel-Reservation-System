package project_hotel;

import java.sql.*;

public class PricingService {

    private static final double TAX_SERVICE_RATE = 0.15;

    public double calculateTotalPrice(int roomTypeId, Date checkIn, Date checkOut, String discountCode) {

        long msPerDay = 24L * 60 * 60 * 1000;
        long nights = (checkOut.getTime() - checkIn.getTime()) / msPerDay;
        if (nights <= 0) nights = 1;

        double basePrice = getBasePrice(roomTypeId);
        double baseTotal = basePrice * nights;

        int month = checkIn.toLocalDate().getMonthValue();
        double seasonalFactor = 1.0;
        if (month == 6 || month == 7 || month == 8) {
            seasonalFactor = 1.20;
        }
        double afterSeason = baseTotal * seasonalFactor;

        double afterDiscount = applyDiscount(afterSeason, discountCode, checkIn);

        double finalTotal = afterDiscount * (1.0 + TAX_SERVICE_RATE);

        return finalTotal;
    }

    private double getBasePrice(int roomTypeId) {
        String sql = "SELECT base_price FROM room_types WHERE type_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private double applyDiscount(double amount, String code, Date refDate) {
        if (code == null || code.isEmpty()) return amount;

        String sql = "SELECT discount_percentage FROM discounts " +
                     "WHERE code = ? " +
                     "AND (start_date IS NULL OR start_date <= ?) " +
                     "AND (end_date IS NULL OR end_date >= ?)";

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setDate(2, refDate);
            ps.setDate(3, refDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double disc = rs.getDouble(1); 
                    return amount * (1.0 - disc);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return amount;
    }
}
