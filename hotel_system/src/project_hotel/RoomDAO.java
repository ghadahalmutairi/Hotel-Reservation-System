package project_hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> findAvailableByType(int typeId, Date checkIn, Date checkOut) {
        List<Room> list = new ArrayList<>();

        String sql =
                "SELECT * FROM rooms r " +
                "WHERE r.type_id = ? " +
                "AND r.status = 'Available' " +
                "AND r.room_id NOT IN ( " +
                "   SELECT room_id FROM reservations " +
                "   WHERE status != 'Canceled' " +
                "   AND NOT (check_out_date <= ? OR check_in_date >= ?) " +
                ")";

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, typeId);
            ps.setDate(2, checkIn);
            ps.setDate(3, checkOut);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Room(
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getInt("type_id"),
                            rs.getString("status")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean isRoomAvailable(int roomId, Date checkIn, Date checkOut) {
        String sql = "SELECT COUNT(*) FROM reservations " +
                     "WHERE room_id = ? AND status != 'Canceled' " +
                     "AND NOT (check_out_date <= ? OR check_in_date >= ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setDate(2, checkIn);
            ps.setDate(3, checkOut);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<String[]> getRoomTypes() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT type_name, type_id FROM room_types ORDER BY type_id";

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("type_name"),
                        String.valueOf(rs.getInt("type_id"))
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number";

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_number"),
                        rs.getInt("type_id"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Room> findByStatus(String status) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE status = ? ORDER BY room_number";

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Room(
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getInt("type_id"),
                            rs.getString("status")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateRoomStatus(int roomId, String newStatus) {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, roomId);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(int roomId, String newStatus) {
        return updateRoomStatus(roomId, newStatus);
    }

}
