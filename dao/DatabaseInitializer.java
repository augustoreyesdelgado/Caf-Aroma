package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String URL =
        "jdbc:sqlite:" + System.getProperty("user.dir") + "/db/cafeteria.db";

    public static void init() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sqlClientes = """
            CREATE TABLE IF NOT EXISTS clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                correo TEXT UNIQUE NOT NULL,
                telefono TEXT
            );
        """;

        String sqlProductos = """
            CREATE TABLE IF NOT EXISTS productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                precio REAL NOT NULL,
                stock INTEGER NOT NULL
            );
        """;

        String sqlVentas = """
            CREATE TABLE IF NOT EXISTS ventas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER,
                total REAL,
                FOREIGN KEY (cliente_id) REFERENCES clientes(id)
            );
        """;

        String sqlItems = """
            CREATE TABLE IF NOT EXISTS items_venta (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                venta_id INTEGER,
                producto_id INTEGER,
                precio REAL,
                cantidad INTEGER,
                FOREIGN KEY (venta_id) REFERENCES ventas(id),
                FOREIGN KEY (producto_id) REFERENCES productos(id)
            );
        """;

        try (Connection conn = DriverManager.getConnection(URL);
             Statement st = conn.createStatement()) {

            st.execute(sqlClientes);
            st.execute(sqlProductos);
            st.execute(sqlVentas);
            st.execute(sqlItems);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
