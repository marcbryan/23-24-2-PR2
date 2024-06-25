package uoc.ds.pr.model;

import edu.uoc.ds.adt.sequential.LinkedList;

import java.util.Comparator;

public class Order {
    public static final Comparator<Order> CMP = Comparator.comparing((Order o) -> o.getClient().getLevel())
                                                          .thenComparing(Order::getOrderNumber);
    private int orderNumber;
    private Client client;
    private Voyage voyage;
    private LinkedList<Product> products;
    private double price;

    public Order(int orderNumber, Client client, Voyage voyage, LinkedList<Product> products, double price) {
        this.orderNumber = orderNumber;
        this.client = client;
        this.voyage = voyage;
        this.products = products;
        this.price = price;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Voyage getVoyage() {
        return voyage;
    }

    public void setVoyage(Voyage voyage) {
        this.voyage = voyage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getOrderNumber() {
        return orderNumber;
    }
}
