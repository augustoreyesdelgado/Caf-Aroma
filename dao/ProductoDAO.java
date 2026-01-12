package dao;

import java.sql.*;
import java.util.*;
import model.Producto;

public class ProductoDAO {

    private static final String URL =
        "jdbc:sqlite:" + System.getProperty("user.dir") + "/db/cafeteria.db";

    public static void guardarProducto(Producto p) {

        String sql = "INSERT INTO productos(nombre, precio, stock) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getStock());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Producto> obtenerProductos() {

        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, precio, stock FROM productos";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Producto(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getDouble("precio"),
                    rs.getInt("stock")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static Producto obtenerPorId(int id) {

        String sql = "SELECT nombre, precio, stock FROM productos WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Producto(
                    id,
                    rs.getString("nombre"),
                    rs.getDouble("precio"),
                    rs.getInt("stock")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void eliminarProductos() {

        String sqlVentas = "DELETE FROM productos";

        try (Connection conn = DriverManager.getConnection(URL);
            Statement st = conn.createStatement()) {

            st.execute(sqlVentas);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
