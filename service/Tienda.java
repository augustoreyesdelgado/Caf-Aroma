package service;

import dao.ProductoDAO;
import dao.VentaDAO;
import model.Cliente;
import model.Producto;
import model.Venta;

public class Tienda {

    public static void registrarCliente(String nombre, String correo, String telefono) {
        Cliente cliente = new Cliente(nombre, correo, telefono);
        // Aquí podrías agregar lógica para guardar el cliente si es necesario
        VentaDAO.guardarCliente(cliente);
    }

    public static void registrarProducto(String nombre, double precio, int stock) {
        Producto p = new Producto(0, nombre, precio, stock);
        ProductoDAO.guardarProducto(p);
    }

    public static void registrarVenta(
            String nombreCliente,
            String correo,
            String telefono,
            int productoId,
            int cantidad
    ) {
        
        System.out.println("Entró al metodo registrar venta");
        
        Cliente cliente = new Cliente(nombreCliente, correo, telefono);
        Venta venta = new Venta(cliente);

        Producto producto;

        try{
            producto = ProductoDAO.obtenerPorId(productoId);
            venta.agregarItem(producto, cantidad);
        }catch (Exception e){
            System.out.println("Error al recuperar producto");
        }


        VentaDAO.guardarVenta(venta);
    }

}
