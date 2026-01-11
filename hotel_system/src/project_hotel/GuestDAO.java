package project_hotel;

import java.sql.*;

public class GuestDAO {

    public int createGuest(String firstName, String lastName,
                           String idNumber, String email, String phone) {

        String sql = "INSERT INTO guests (first_name, last_name, id_number, email, phone) " +
                     "VALUES (?,?,?,?,?)";

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);

            if (idNumber == null || idNumber.trim().isEmpty())
                ps.setNull(3, Types.VARCHAR);
            else
                ps.setString(3, idNumber);

            if (email == null || email.trim().isEmpty())
                ps.setNull(4, Types.VARCHAR);
            else
                ps.setString(4, email);

            if (phone == null || phone.trim().isEmpty())
                ps.setNull(5, Types.VARCHAR);
            else
                ps.setString(5, phone);


            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);   
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; 
    }
}
