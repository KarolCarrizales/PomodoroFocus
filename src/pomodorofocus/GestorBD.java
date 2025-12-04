package pomodorofocus;

import java.sql.*;
import java.util.ArrayList;

public class GestorBD {
    
    private static final String URL = "jdbc:sqlite:pomodoro_data.db";

    public static void inicializarBaseDeDatos() {
        String sql = "CREATE TABLE IF NOT EXISTS tareas (id INTEGER PRIMARY KEY AUTOINCREMENT, descripcion TEXT NOT NULL)";
        
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertarTarea(String descripcion) {
        String sql = "INSERT INTO tareas(descripcion) VALUES(?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, descripcion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void eliminarTarea(String descripcion) {
        String sql = "DELETE FROM tareas WHERE descripcion = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, descripcion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> obtenerTareas() {
        ArrayList<String> lista = new ArrayList<>();
        String sql = "SELECT descripcion FROM tareas";
        
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                lista.add(rs.getString("descripcion"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}