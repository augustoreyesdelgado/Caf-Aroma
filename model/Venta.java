package model;

import java.util.ArrayList;
import java.util.List;

public class Venta {
    private Cliente cliente;
    private List<ItemVenta> items = new ArrayList<>();

    public Venta(Cliente cliente) {
        this.cliente = cliente;
    }

    public void agregarItem(Producto producto, int cantidad) {
        if (producto.reducirStock(cantidad)) {
            items.add(new ItemVenta(producto, cantidad));
        } else {
            System.out.println("Stock insuficiente de " + producto.getNombre());
        }
    }

    public double calcularTotal() {
        double total = 0;
        for (ItemVenta item : items) {
            total += item.calcularSubtotal();
        }
        return total;
    }

    public Cliente getCliente() {
        return cliente;
    }
    
    public List<ItemVenta> getItems() {
        return items;
    }
}