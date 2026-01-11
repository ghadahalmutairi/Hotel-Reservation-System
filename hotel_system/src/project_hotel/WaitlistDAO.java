/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package project_hotel;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WaitlistDAO {

    public void addToWaitlist(int guestId, int roomTypeId,
                              Date checkIn, Date checkOut) {
        String sql = "INSERT INTO waitlist (guest_id, room_type_id, check_in_date, check_out_date) " +
                     "VALUES (?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            ps.setInt(2, roomTypeId);
            ps.setDate(3, checkIn);
            ps.setDate(4, checkOut);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
