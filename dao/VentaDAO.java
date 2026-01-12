package dao;

import java.sql.*;
import java.util.*;

import model.Cliente;
import model.ItemVenta;
import model.Venta;

public class VentaDAO {

    private static final String URL = "jdbc:sqlite:db/cafeteria.db";

    public static void guardarVenta(Venta venta) {

        int clienteId = guardarCliente(venta.getCliente());

        String sqlVenta = "INSERT INTO ventas(cliente_id, total) VALUES (?, ?)";
        String sqlItem = "INSERT INTO items_venta(venta_id, producto_id, precio, cantidad) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            PreparedStatement psVenta =
                conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);

            psVenta.setInt(1, clienteId);
            psVenta.setDouble(2, venta.calcularTotal());
            psVenta.executeUpdate();

            ResultSet rs = psVenta.getGeneratedKeys();
            rs.next();
            int ventaId = rs.getInt(1);

            for (ItemVenta item : venta.getItems()) {
                PreparedStatement psItem = conn.prepareStatement(sqlItem);
                psItem.setInt(1, ventaId);
                psItem.setInt(2, item.getProducto().getId());
                psItem.setDouble(3, item.getProducto().getPrecio());
                psItem.setInt(4, item.getCantidad());
                psItem.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al guardar la venta.");
        }
    }

    public static int guardarCliente(Cliente cliente) {

        String sql = "INSERT INTO clientes(nombre, correo, telefono) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getCorreo());
            ps.setString(3, cliente.getTelefono());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            // Cliente ya existe (correo duplicado)
            if (e.getMessage().contains("UNIQUE")) {
                return obtenerClienteIdPorCorreo(cliente.getCorreo());
            }
            e.printStackTrace();
        }

        return -1;
    }

    private static int obtenerClienteIdPorCorreo(String correo) {

        String sql = "SELECT id FROM clientes WHERE correo = ?";

        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, correo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static List<String> obtenerVentas() {
        List<String> lista = new ArrayList<>();
        String sql = """
            SELECT 
                c.nombre AS cliente,
                GROUP_CONCAT(p.nombre || ' x' || i.cantidad, ', ') AS productos,
                v.total
            FROM ventas v
            JOIN clientes c ON v.cliente_id = c.id
            JOIN items_venta i ON v.id = i.venta_id
            JOIN productos p ON i.producto_id = p.id
            GROUP BY v.id, c.nombre, v.total
            ORDER BY v.id DESC
        """;

        try (Connection conn = DriverManager.getConnection(URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(
                rs.getString("cliente") + " — " +
                rs.getString("productos") +
                " — $" + rs.getDouble("total")
                );    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static void limpiarVentas() {

        String sqlItems = "DELETE FROM items_venta";
        String sqlVentas = "DELETE FROM ventas";

        try (Connection conn = DriverManager.getConnection(URL);
            Statement st = conn.createStatement()) {

            st.execute(sqlItems);
            st.execute(sqlVentas);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}