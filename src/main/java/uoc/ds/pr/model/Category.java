package uoc.ds.pr.model;

import edu.uoc.ds.adt.nonlinear.DictionaryAVLImpl;
import edu.uoc.ds.traversal.Iterator;

public class Category {
    private String id;
    private String name;
    private DictionaryAVLImpl<String, Product> products;

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
        products = new DictionaryAVLImpl<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void update(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public int numProducts() {
        return products.size();
    }

    public Iterator<Product> products() {
        return products.values();
    }

    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }
}
