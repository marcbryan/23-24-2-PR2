package uoc.ds.pr;

import java.util.Date;

import edu.uoc.ds.adt.nonlinear.DictionaryAVLImpl;
import edu.uoc.ds.adt.nonlinear.HashTable;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.adt.sequential.List;
import edu.uoc.ds.traversal.Iterator;
import uoc.ds.pr.exceptions.*;
import uoc.ds.pr.model.*;
import uoc.ds.pr.util.DSArray;
import uoc.ds.pr.util.OrderedVector;


public class ShippingLineImpl implements ShippingLine {

    private DSArray<Ship> ships;
    // Cambio de estructura de datos en estos 3 atributos
    private HashTable<String, Route> routes;
    private DictionaryAVLImpl<String, Client> clients;
    private DictionaryAVLImpl<String, Voyage> voyages;

    private OrderedVector<Client>  bestClient;
	private OrderedVector<Route> bestRoute;

    private HashTable<String, Port> ports;



    public ShippingLineImpl() {
        ships = new DSArray<>(MAX_NUM_SHIPS);
        routes = new HashTable<>();
        clients = new DictionaryAVLImpl<>();
        voyages = new DictionaryAVLImpl<>();
        bestClient = new OrderedVector<>(MAX_CLIENTS, Client.CMP_V);
        bestRoute = new OrderedVector<>(MAX_NUM_ROUTES, Route.CMP_V);
        ports = new HashTable<>();
    }


    @Override
    public void addShip(String id, String name, int nArmChairs, int nCabins2, int nCabins4, int nParkingLots, int unLoadTimeinMinutes) {
        Ship ship = getShip(id);
        if (ship == null) {
            ship = new Ship(id, name, nArmChairs, nCabins2, nCabins4, nParkingLots, unLoadTimeinMinutes);
            this.ships.put(id, ship);
        }
        else {
            ship.update(name, nArmChairs, nCabins2, nCabins4, nParkingLots, unLoadTimeinMinutes);
        }
    }



    @Override
    public void addRoute(String id, String beginningPort, String arrivalPort, double kms) throws SrcPortNotFoundException, DstPortNotFoundException, RouteAlreadyExistException {
        Route route = getRoute(id);
        Port srcPort = getPort(beginningPort);
        Port dstPort = getPort(arrivalPort);

        if (srcPort == null)
            throw new SrcPortNotFoundException();

        if (dstPort == null)
            throw new DstPortNotFoundException();

        Iterator<Route> iterator = routes.values();
        while (iterator.hasNext()) {
            Route foundRoute = iterator.next();
            // Si ya existe la ruta y se intenta añadirla con otro id, lanzamos la excepción
            if (foundRoute.getBeginningPort().equals(beginningPort) && foundRoute.getArrivalPort().equals(arrivalPort))
                throw new RouteAlreadyExistException();
        }

        if (route == null) {
            route = new Route(id, srcPort, dstPort, kms);
            this.routes.put(id, route);
        }
        else
            route.update(srcPort, dstPort);
    }

    public void addClient(String id, String name, String surname) {

        Client client = getClient(id);
        if (client == null) {
            client = new Client(id, name, surname);
            clients.put(id, client);
        }
        else {
            client.update(name, surname);
        }
    }


    @Override
    public void addVoyage(String id, Date departureDt, Date arrivalDt, String idShip, String idRoute) throws ShipNotFoundException, RouteNotFoundException {

        Ship ship = getShip(idShip);
        if (ship == null) {
            throw new ShipNotFoundException();
        }

        Route route = getRoute(idRoute);
        if (route == null) {
            throw new RouteNotFoundException();
        }

        Voyage voyage = getVoyage(id);
        if (voyage == null) {
            voyage = new Voyage(id, departureDt, arrivalDt, ship, route);
            voyages.put(id, voyage);
            route.addVoyage(voyage);
            updateBestRoute(voyage.getRoute());
        }
        else {
            if (!idRoute.equals(voyage.getRoute().getId())) {
                Route oldRoute = voyage.getRoute();
                oldRoute.remove(voyage);
                route.addVoyage(voyage);
            }
            voyage.update(departureDt, arrivalDt, ship, route);
        }

    }


    public void reserve(String[] clients, String idVoyage, AccommodationType accommodationType, String idVehicle, double price)
            throws ClientNotFoundException, VoyageNotFoundException, ParkingFullException,
            NoAcommodationAvailableException, MaxExceededException, ReservationAlreadyExistsException {

        boolean parkingReserve = false;
        Voyage voyage = getVoyage(idVoyage);
        if (voyage == null) {
            throw new VoyageNotFoundException();
        }
        List<Client> clientLinkedList = getAllClients(clients, voyage);
        if (accommodationType == AccommodationType.CABIN2 && clientLinkedList.size()>MAX_PEOPLE_CABIN2) {
            throw new MaxExceededException();
        }
        else if (accommodationType == AccommodationType.CABIN4 && clientLinkedList.size()>MAX_PEOPLE_CABIN4) {
            throw new MaxExceededException();
        }


        parkingReserve = (idVehicle!=null && !voyage.parkingFull());

        if (voyage.parkingFull()) {
            throw new ParkingFullException();
        }
        Reservation reservation = null;

        if (voyage.isAcommodationAvailable(accommodationType, clientLinkedList.size())) {
            voyage.addReservation(clientLinkedList, accommodationType, price, idVehicle);
        }
        else {
            throw new NoAcommodationAvailableException();
        }

    }

    public void load(String idClient, String idVoyage, Date dt) throws
            LoadingAlreadyException, ClientNotFoundException, VoyageNotFoundException, ReservationNotFoundException {
        Client client = getClient(idClient);
        if (client == null) {
            throw new ClientNotFoundException();
        }

        Voyage voyage = getVoyage(idVoyage);
        if (voyage == null) {
            throw new VoyageNotFoundException();
        }

        Reservation reservation = client.findReservation(idVoyage);
        if (reservation == null) {
            throw new ReservationNotFoundException();
        }

        if (reservation.isLoaded()) {
            throw new LoadingAlreadyException();
        }
        reservation.loaded();
        if (reservation.hasParkingLot()) {
            reservation.loadVehicle();
            client.addVoyage(reservation.getVoyage());
            updateBestClient(client);
        }
    }

    public Iterator<Reservation> unload(String idVoyage) throws VoyageNotFoundException {
        Voyage voyage = getVoyage(idVoyage);
        if (voyage == null) {
            throw new VoyageNotFoundException();
        }
        Ship ship = voyage.getShip();
        int unLoadTimeInMinutes = ship.getUnLoadTimeInMinutes();
        Iterator<Reservation> it = voyage.parking();
        Reservation reservation = null;
        while (it.hasNext()) {
            reservation = it.next();
            reservation.setUnLoadTimeInMinutes(unLoadTimeInMinutes);
            unLoadTimeInMinutes += ship.getUnLoadTimeInMinutes();
        }
        voyage.disembarked();

        return voyage.parking();
    }


    public int unloadTime(String idVehicle, String idVoyage) throws LandingNotDoneException,VoyageNotFoundException, VehicleNotFoundException {
        Voyage voyage = getVoyage(idVoyage);
        if (voyage == null) {
            throw new VoyageNotFoundException();
        }

        if (!voyage.hasDisembarked()) {
            throw new LandingNotDoneException();
        }

        Iterator<Reservation> it = voyage.parking();
        Reservation reservation = null;
        boolean found = false;

        while (it.hasNext() && !found) {
            reservation = it.next();
            found = reservation.getIdVehicle().equals(idVehicle);
        }
        if (!found) {
            throw new VehicleNotFoundException();
        }

        return (reservation.getUnLoadTimeInMinutes());
    }


    public Iterator<Reservation> getClientReservations(String idClient) throws NoReservationException {
        Client client = getClient(idClient);
        Iterator<Reservation> it = client.reservations();
        if (!it.hasNext()) throw new NoReservationException();

        return it;
    }

    @Override
    public Iterator<Reservation> getVoyageReservations(String idVoyage) throws NoReservationException {
        Voyage voyage = getVoyage(idVoyage);
        Iterator<Reservation> it = voyage.reservations();
        if (!it.hasNext()) throw new NoReservationException();

        return it;
    }

    public Iterator<Reservation> getAccommodationReservations(String idVoyage, AccommodationType accommodationType) throws NoReservationException {
        Voyage voyage = getVoyage(idVoyage);
        Iterator<Reservation> it = voyage.reservations(accommodationType);
        if (!it.hasNext()) throw new NoReservationException();

        return it;

    }

    public Client getMostTravelerClient() throws NoClientException {
        if (bestClient.isEmpty()) {
            throw new NoClientException();
        }
        return bestClient.elementAt(0);
    }

    public Route getMostTraveledRoute() throws NoRouteException {
        if (bestRoute.isEmpty()) {
            throw new NoRouteException();
        }
        return bestRoute.elementAt(0);
    }

    private void updateBestClient(Client client) {
        bestClient.delete(client);
        bestClient.update(client);

    }

    private void updateBestRoute(Route route) {
        bestRoute.delete(route);
        bestRoute.update(route);
    }


    private List<Client> getAllClients(String[] clients, Voyage voyage) throws ClientNotFoundException, ReservationAlreadyExistsException{
        LinkedList<Client> clientLinkedList = new LinkedList<>();
        Client client = null;
        for (String sClient : clients) {
            client = getClient(sClient);
            if (client == null) {
                throw new ClientNotFoundException();
            }
            if (!client.hasReservation(voyage)) {
                clientLinkedList.insertEnd(client);
            }
            else {
                throw new ReservationAlreadyExistsException();
            }
        }
        return clientLinkedList;
    }

    /***********************************************************************************/
    /******************** AUX OPERATIONS  **********************************************/
    /***********************************************************************************/


    public Ship getShip(String id) {
        return ships.get(id);
    }

    @Override
    public Port getPort(String id) {
        return ports.get(id);
    }

    public Client getClient(String id) {
        return clients.get(id);
    }

    public Route getRoute(String idRoute) {
        return routes.get(idRoute);
    }

    public Voyage getVoyage(String id) {
        return voyages.get(id);
    }

    public int numShips() {
        return ships.size();
    }

    public int numRoutes() {
        return routes.size();
    }

    @Override
    public int numPorts() {
        return -1;
    }

    public int numClients() {
        return clients.size();
    }

    public int numVoyages() {return voyages.size(); }

    protected DictionaryAVLImpl<String, Voyage> getVoyages() {
        return voyages;
    }

    protected HashTable<String, Route> getRoutes() {
        return routes;
    }

    protected HashTable<String, Port> getPorts() {
        return ports;
    }
}
