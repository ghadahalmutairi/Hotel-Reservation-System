package project_hotel;

import java.sql.*;

public class ReservationDAO {

    private ActivityLogDAO logDAO = new ActivityLogDAO();

    public boolean createReservation(int guestId, int roomId,
                                     Date checkIn, Date checkOut,
                                     double totalPrice,
                                     int userId,
                                     Integer discountId) {
        String sql = "INSERT INTO reservations " +
                "(guest_id, room_id, check_in_date, check_out_date, total_price, status, deposit_amount, discount_id) " +
                "VALUES (?, ?, ?, ?, ?, 'Confirmed', 0.00, ?)";

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, guestId);
            ps.setInt(2, roomId);
            ps.setDate(3, checkIn);
            ps.setDate(4, checkOut);
            ps.setDouble(5, totalPrice);

            if (discountId == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, discountId);
            }

            int rows = ps.executeUpdate();
            if (rows == 1) {
                logDAO.log(userId, "Created reservation for guest " + guestId + " room " + roomId);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkIn(int reservationId, double deposit, int userId) {
        String updateReservation =
                "UPDATE reservations SET status='Checked-In', deposit_amount=? WHERE reservation_id=?";

        String updateRoom =
                "UPDATE rooms SET status='Occupied' " +
                "WHERE room_id = (SELECT room_id FROM reservations WHERE reservation_id=?)";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updateReservation)) {
                ps.setDouble(1, deposit);
                ps.setInt(2, reservationId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateRoom)) {
                ps.setInt(1, reservationId);
                ps.executeUpdate();
            }

            conn.commit();
            logDAO.log(userId, "Check-In reservation " + reservationId);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean checkOut(int reservationId,
                            double amountPaid,
                            double lateFee,
                            double damages,
                            int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            String select = "SELECT room_id, total_price, deposit_amount FROM reservations WHERE reservation_id=?";
            int roomId = 0;
            double total = 0;
            double dep = 0;

            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, reservationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        roomId = rs.getInt("room_id");
                        total = rs.getDouble("total_price");
                        dep = rs.getDouble("deposit_amount");
                    }
                }
            }

            double extra = lateFee + damages;
            double grandTotal = total + extra;
            double totalPaid = dep + amountPaid;
            double remaining = grandTotal - totalPaid;

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE reservations SET status='Checked-Out', total_price=? WHERE reservation_id=?")) {
                ps.setDouble(1, grandTotal);
                ps.setInt(2, reservationId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE rooms SET status='Cleaning Required' WHERE room_id=?")) {
                ps.setInt(1, roomId);
                ps.executeUpdate();
            }

            // إنشاء Invoice
            int invoiceId = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO invoices (reservation_id, total_charges, total_paid, balance, status, processed_by_user_id) " +
                            "VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, reservationId);
                ps.setDouble(2, grandTotal);
                ps.setDouble(3, totalPaid);
                ps.setDouble(4, remaining);
                ps.setString(5, remaining <= 0 ? "Paid" : "Pending");
                ps.setInt(6, userId);

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoiceId = rs.getInt(1);
                    }
                }
            }

            // إضافة Payment بالمبلغ المدفوع الآن
            if (amountPaid > 0 && invoiceId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO payments (invoice_id, amount, payment_method, recorded_by_user_id) " +
                                "VALUES (?,?,?,?)")) {
                    ps.setInt(1, invoiceId);
                    ps.setDouble(2, amountPaid);
                    ps.setString(3, "Cash");
                    ps.setInt(4, userId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            logDAO.log(userId, "Check-Out reservation " + reservationId +
                    ", extra charges=" + extra + ", remaining=" + remaining);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean cancelReservation(int reservationId, int userId) {
        String sql = "UPDATE reservations SET status='Canceled' WHERE reservation_id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                logDAO.log(userId, "Canceled reservation " + reservationId);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean changeDates(int reservationId, Date newCheckIn, Date newCheckOut, int userId) {
        String sql = "UPDATE reservations SET check_in_date=?, check_out_date=? WHERE reservation_id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, newCheckIn);
            ps.setDate(2, newCheckOut);
            ps.setInt(3, reservationId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                logDAO.log(userId, "Changed dates of reservation " + reservationId);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}
