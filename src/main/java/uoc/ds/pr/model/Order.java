package uoc.ds.pr.model;

import edu.uoc.ds.adt.sequential.LinkedList;

import java.util.Comparator;

public class Order {
    public static final Comparator<Order> CMP = Comparator.comparing(o -> o.getClient().getLevel());
    private Client client;
    private Voyage voyage;
    private LinkedList<Product> products;
    private double price;

    public Order(Client client, Voyage voyage, double price) {
        this.client = client;
        this.voyage = voyage;
        this.price = price;
        products = new LinkedList<>();
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
}
