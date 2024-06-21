package uoc.ds.pr;

import edu.uoc.ds.traversal.Iterator;
import uoc.ds.pr.exceptions.*;
import uoc.ds.pr.model.*;

public class ShippingLinePR2Impl extends ShippingLineImpl implements ShippingLinePR2 {
    // TODO: Implementar métodos
    @Override
    public void addPort(String id, String name, String imageUrl, String description) {

    }

    @Override
    public void addCategory(String id, String name) {

    }

    @Override
    public void addProduct(String id, String name, String description, String idCategory) throws CategoryNotFoundException {

    }

    @Override
    public Iterator<Product> getProductsByCategory(String categoryId) throws CategoryNotFoundException, NoProductsException {
        return null;
    }

    @Override
    public void linkProduct(String productId, String shipId) throws ProductNotFoundException, ShipNotFoundException, ProductAlreadyOnMenuException {

    }

    @Override
    public void unlinkProduct(String productId, String shipId) throws ProductNotFoundException, ShipNotFoundException, ProductNotInMenuException {

    }

    @Override
    public Iterator<Product> getVoyageProductsByCategory(String voyageId, String categoryId) throws VoyageNotFoundException, CategoryNotFoundException, NoProductsException {
        return null;
    }

    @Override
    public void makeOrder(String clientId, String voyageId, String[] products, double price) throws ClientNotFoundException, VoyageNotFoundException, ProductNotFoundException, ProductNotInMenuException, ClientIsNotInVoyageException {

    }

    @Override
    public Order serveOrder(String voyageId) throws VoyageNotFoundException, NoOrdersException {
        return null;
    }

    @Override
    public Iterator<Order> getOrdersByClient(String clientId) throws ClientNotFoundException, NoOrdersException {
        return null;
    }

    @Override
    public Iterator<Order> getOrdersByShip(String shipId) throws ShipNotFoundException, NoOrdersException {
        return null;
    }

    @Override
    public LoyaltyLevel getLevel(String clientId) throws ClientNotFoundException {
        return null;
    }

    @Override
    public Iterator<Route> getRoutesByOrigin(String portId) throws NoRouteException {
        return null;
    }

    @Override
    public Route getRoute(String idBeginningPort, String idArrivalPort) throws NoRouteException, SamePortException {
        return null;
    }

    @Override
    public Iterator<Voyage> getVoyagesByRoute(String routeId) throws NoVoyagesException {
        return null;
    }

    @Override
    public Iterator<Client> best5Clients() throws NoClientException {
        return null;
    }

    @Override
    public boolean existsRouteBetween(String idAPort, String idBPort) throws SamePortException, SrcPortNotFoundException, DstPortNotFoundException {
        return false;
    }

    @Override
    public Iterator<Route> getBestKmsRoute(String idAPort, String idBPort) throws SamePortException, SrcPortNotFoundException, DstPortNotFoundException, NoRouteException {
        return null;
    }

    @Override
    public Iterator<Route> getBestPortsRoute(String idAPort, String idBPort) throws SamePortException, SrcPortNotFoundException, DstPortNotFoundException, NoRouteException {
        return null;
    }

    @Override
    public int numPorts() {
        return 0;
    }

    @Override
    public int numCategories() {
        return 0;
    }

    @Override
    public int numProducts() {
        return 0;
    }

    @Override
    public int numProducts(String categoryId) {
        return 0;
    }

    @Override
    public int numProductsByShip(String shipId) {
        return 0;
    }

    @Override
    public int numOrders(String voyageId) {
        return 0;
    }

    @Override
    public Category getCategory(String id) {
        return null;
    }
}
