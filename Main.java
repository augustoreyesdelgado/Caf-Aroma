import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import dao.DatabaseInitializer;
import dao.ProductoDAO;
import dao.VentaDAO;
import model.Producto;
import service.Tienda;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {

        DatabaseInitializer.init();

        int port = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080")
        );

        HttpServer server = HttpServer.create(
            new InetSocketAddress(port), 0
        );


        /* Endpoints */
        // Página principal
        server.createContext("/", e -> {
            try {
                enviarHTMLConHeader(e, "web/index.html");
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        // Página de orden
        server.createContext("/orden", e -> {
            try {
                enviarOrden(e);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        // Página del menú
        server.createContext("/menu", e -> {
            try {
                enviarMenu(e);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        // Página de inventario
        server.createContext("/inventario", e -> {
            try {
                enviarHTMLConHeader(e, "web/inventario.html");
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        // Registrar un producto
        server.createContext("/registrarProducto", e -> {

            if (!e.getRequestMethod().equalsIgnoreCase("POST")) {
                e.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(e.getRequestBody().readAllBytes());

            String[] params = body.split("&");

            String producto = URLDecoder.decode(params[0].split("=")[1], StandardCharsets.UTF_8);
            double precio = Double.parseDouble(params[1].split("=")[1]);
            int cantidad = Integer.parseInt(params[2].split("=")[1]);

            Tienda.registrarProducto(producto, precio, cantidad);

            e.getResponseHeaders().add("Location", "/orden");
            e.sendResponseHeaders(302, -1);
            e.close();
        });

        // Registrar una venta
        server.createContext("/registrar", e -> {

            System.out.println("Entró a registrar venta");
            if (!e.getRequestMethod().equalsIgnoreCase("POST")) {
                e.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(e.getRequestBody().readAllBytes());

            String[] params = body.split("&");

            String cliente = URLDecoder.decode(params[0].split("=")[1], StandardCharsets.UTF_8);
            String correo  = URLDecoder.decode(params[1].split("=")[1], StandardCharsets.UTF_8);
            String telefono  = URLDecoder.decode(params[2].split("=")[1], StandardCharsets.UTF_8);
            int productoId = Integer.parseInt(params[3].split("=")[1]);
            int cantidad = Integer.parseInt(params[4].split("=")[1]);

            Tienda.registrarVenta(cliente, correo, telefono, productoId, cantidad);

            e.getResponseHeaders().add("Location", "/ventas");
            e.sendResponseHeaders(302, -1);
            e.close();
        });

        // Historial de ventas
        server.createContext("/ventas", e -> {
            try {
                enviarHistorial(e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Recursos estáticos
        server.createContext("/estilos", e -> {
            try {
                enviarArchivo(e, "web/css/estilos.css");
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        // Contacto
        server.createContext("/contacto", e -> {
            try {
                enviarHTMLConHeader(e, "web/contacto.html");
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        // Limpiar historial de ventas
        server.createContext("/limpiar", e -> {
            VentaDAO.limpiarVentas();
            e.getResponseHeaders().add("Location", "/ventas");
            try {
                e.sendResponseHeaders(302, -1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.close();
        });

        // Eliminar productos del inventario
        server.createContext("/EliminarProductos", e -> {
            ProductoDAO.eliminarProductos();
            e.getResponseHeaders().add("Location", "/menu");
            try {
                e.sendResponseHeaders(302, -1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.close();
        });

        // Rutas para imágenes
        server.createContext("/img/", e -> {
            try {
                String rutaSolicitada = e.getRequestURI().getPath();
                // Ej: /img/cafe-bienvenida.png

                String rutaArchivo = "web/assets" + rutaSolicitada;
                enviarArchivo(e, rutaArchivo);

            } catch (Exception ex) {
                e.sendResponseHeaders(404, -1);
                e.close();
            }
        });

        // Iniciar el servidor
        server.start();
        System.out.println("Servidor activo en http://localhost:8080");
    }

    // Métodos auxiliares
    // Enviar un archivo estático
    static void enviarArchivo(HttpExchange e, String ruta) throws Exception {
        byte[] data = Files.readAllBytes(Path.of(ruta));
        e.sendResponseHeaders(200, data.length);
        e.getResponseBody().write(data);
        e.close();
    }

    // Enviar orden
    static void enviarOrden(HttpExchange e) throws Exception {

        Path base = Path.of(System.getProperty("user.dir"));

        String html = Files.readString(base.resolve("web/orden.html"));
        String header = Files.readString(base.resolve("web/header.html"));

        StringBuilder opciones = new StringBuilder();

        for (Producto p : ProductoDAO.obtenerProductos()) {
            opciones.append("<option value=\"" + p.getId() + "\">\n");
            opciones.append("    " + p.getNombre() + " ($" + p.getPrecio() + ")\n");
            opciones.append("</option>\n");
        }

        html = html.replace("{{header}}", header);
        html = html.replace("{{opcionesProductos}}", opciones.toString());

        byte[] data = html.getBytes();
        e.getResponseHeaders().set("Content-Type", "text/html");
        e.sendResponseHeaders(200, data.length);
        e.getResponseBody().write(data);
        e.close();
    }

    // Enviar un archivo HTML con el header incluido
    static void enviarHTMLConHeader(HttpExchange e, String rutaHtml) throws Exception {

        Path base = Path.of(System.getProperty("user.dir"));

        String html = Files.readString(base.resolve(rutaHtml));
        String header = Files.readString(base.resolve("web/header.html"));

        html = html.replace("{{header}}", header);

        byte[] data = html.getBytes();

        e.getResponseHeaders().set("Content-Type", "text/html");
        e.sendResponseHeaders(200, data.length);
        e.getResponseBody().write(data);
        e.close();
    }

    // Enviar el historial de ventas
    static void enviarHistorial(HttpExchange e) throws Exception {

        Path base = Path.of(System.getProperty("user.dir"));

        String html = Files.readString(base.resolve("web/historial.html"));
        String header = Files.readString(base.resolve("web/header.html"));

        StringBuilder lista = new StringBuilder();

        for (String v : VentaDAO.obtenerVentas()) {
            lista.append("""
                <li class="list-group-item">
                    """ + v + """
                </li>
            """);
        }

        html = html.replace("{{header}}", header);
        html = html.replace("{{listaVentas}}", lista.toString());

        byte[] data = html.getBytes();

        e.getResponseHeaders().set("Content-Type", "text/html");
        e.sendResponseHeaders(200, data.length);
        e.getResponseBody().write(data);
        e.close();
    }

    static void enviarMenu(HttpExchange e) throws Exception {

        Path base = Path.of(System.getProperty("user.dir"));

        String html = Files.readString(base.resolve("web/menu.html"));
        String header = Files.readString(base.resolve("web/header.html"));

        StringBuilder cards = new StringBuilder();

        for (var p : dao.ProductoDAO.obtenerProductos()) {

            cards.append("""
                <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-img-container">
                    <img src="/img/producto-default.png"
                        class="card-img-top"
                        alt="%s">
                    </div>
                    <div class="card-body text-center">
                    <h5 class="card-title">%s</h5>
                    <p class="text-primary fw-bold">
                        Precio: $%s
                    </p>
                    </div>
                </div>
                </div>
            """.formatted(p.getNombre(), p.getNombre(), p.getPrecio()));
        }

        html = html.replace("{{header}}", header);
        html = html.replace("{{cardsProductos}}", cards.toString());

        byte[] data = html.getBytes();

        e.getResponseHeaders().set("Content-Type", "text/html");
        e.sendResponseHeaders(200, data.length);
        e.getResponseBody().write(data);
        e.close();
    }
}
