package uoc.ds.pr.model;

import edu.uoc.ds.adt.helpers.Position;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.adt.sequential.List;
import edu.uoc.ds.traversal.Iterator;
import edu.uoc.ds.traversal.Traversal;

public class Ship {

    private String id;
    private String name;
    private int nArmChairs;
    private int nCabins2;
    private int nCabins4;
    private int nParkingLots;


    private int unLoadTimeinMinutes;

    private List<Product> linkedProducts;

    public Ship(String id, String name, int nArmChairs, int nCabins2, int nCabins4, int nParkingLots, int unLoadTimeinMinutes) {
        setId(id);
        update(name, nArmChairs, nCabins2, nCabins4, nParkingLots, unLoadTimeinMinutes);
        linkedProducts = new LinkedList<>();
    }


    public void update(String name, int nArmChairs, int nCabins2, int nCabins4, int nParkingLots, int
            unLoadTimeinMinutes) {
        this.setName(name);
        this.setnArmChairs(nArmChairs);
        this.setnCabins2(nCabins2);
        this.setnCabins4(nCabins4);
        this.setnParkingLots(nParkingLots);
        this.setUnLoadTime(unLoadTimeinMinutes);
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnLoadTime(int unLoadTime) {
        this.unLoadTimeinMinutes = unLoadTime;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getUnLoadTimeInMinutes() {
        return unLoadTimeinMinutes;
    }
    public int getnArmChairs() {
        return nArmChairs;
    }

    public void setnArmChairs(int nArmChairs) {
        this.nArmChairs = nArmChairs;
    }

    public int getnCabins2() {
        return nCabins2;
    }

    public void setnCabins2(int nCabins2) {
        this.nCabins2 = nCabins2;
    }

    public int getnCabins4() {
        return nCabins4;
    }

    public void setnCabins4(int nCabins4) {
        this.nCabins4 = nCabins4;
    }

    public int getnParkingSlots() {
        return nParkingLots;
    }

    public void setnParkingLots(int nParkingLots) {
        this.nParkingLots = nParkingLots;
    }

    public Iterator<Product> linkedProducts() {
        return linkedProducts.values();
    }

    public void linkProduct(Product product) {
        linkedProducts.insertEnd(product);
    }

    public Traversal<Product> linkedProductsPositions() {
        return linkedProducts.positions();
    }

    public void deleteLinkedProduct(Position<Product> pos) {
        linkedProducts.delete(pos);
    }

    public int numLinkedProducts() {
        return linkedProducts.size();
    }
}