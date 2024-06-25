package uoc.ds.pr;

import edu.uoc.ds.adt.helpers.Position;
import edu.uoc.ds.adt.nonlinear.HashTable;
import edu.uoc.ds.adt.nonlinear.graphs.DirectedEdge;
import edu.uoc.ds.adt.nonlinear.graphs.DirectedGraphImpl;
import edu.uoc.ds.adt.nonlinear.graphs.Edge;
import edu.uoc.ds.adt.nonlinear.graphs.Vertex;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.adt.sequential.List;
import edu.uoc.ds.traversal.Iterator;
import edu.uoc.ds.traversal.Traversal;
import uoc.ds.pr.exceptions.*;
import uoc.ds.pr.model.*;
import uoc.ds.pr.util.OrderedVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShippingLinePR2Impl extends ShippingLineImpl implements ShippingLinePR2 {
    private int orderNumber;
    private HashTable<String, Port> ports;
    private List<Category> categories;
    private HashTable<String, Product> products;
    private OrderedVector<Client> best5Clients;

    public ShippingLinePR2Impl() {
        super();

        orderNumber = 1;
        ports = getPorts();
        categories = new LinkedList<>();
        products = new HashTable<>();
        best5Clients = new OrderedVector<>(MAX_CLIENTS, Client.CMP_O);
    }

    @Override
    public void addPort(String id, String name, String imageUrl, String description) {
        Port port = getPort(id);
        // Comprobamos si existe el puerto
        if (port == null) {
            port = new Port(id, name, imageUrl, description);
            ports.put(id, port);
        }
        else
           // Si ya existe, actualizamos los datos
           port.update(id, name, imageUrl, description);
    }

    @Override
    public void addCategory(String id, String name) {
        Category category = getCategory(id);
        // Comprobamos si existe la categoría
        if (category == null) {
            category = new Category(id, name);
            categories.insertEnd(category);
        }
        else
            // Si ya existe, actualizamos los datos
            category.update(id, name);
    }

    @Override
    public void addProduct(String id, String name, String description, String idCategory) throws CategoryNotFoundException {
        Category category = getCategory(idCategory);
        // Si la categoría no existe, lanzamos la excepción
        if (category == null)
            throw new CategoryNotFoundException();

        Product product = products.get(id);
        // Comprobamos si existe el producto
        if (product == null) {
            product = new Product(id, name, description, category);
            products.put(id, product);
        }
        else
            // Si ya existe, actualizamos los datos
            product.update(id, name, description, category);

        // Añadimos el producto a su categoría
        category.addProduct(product);
    }

    @Override
    public Iterator<Product> getProductsByCategory(String categoryId) throws CategoryNotFoundException, NoProductsException {
        Category category = getCategory(categoryId);
        // Si la categoría no existe, lanzamos la excepción
        if (category == null)
            throw new CategoryNotFoundException();

        // Si la categoría no tiene productos, lanzamos la excepción
        if (category.numProducts() == 0)
            throw new NoProductsException();

        return category.products();
    }

    @Override
    public void linkProduct(String productId, String shipId) throws ProductNotFoundException, ShipNotFoundException, ProductAlreadyOnMenuException {
        Product product = products.get(productId);
        // Si el producto no existe, lanzamos la excepción
        if (product == null)
            throw new ProductNotFoundException();

        Ship ship = getShip(shipId);
        // Si el buque no existe, lanzamos la excepción
        if (ship == null)
            throw new ShipNotFoundException();

        // Comprobamos si el producto ya está asignado en el buque
        Iterator<Product> iterator = ship.linkedProducts();
        while (iterator.hasNext()) {
            Product linkedProduct = iterator.next();

            // Si el producto ya está asignado, lanzamos la excepción
            if (linkedProduct.getId().equals(productId))
                throw new ProductAlreadyOnMenuException();
        }

        // Añadimos el producto al buque
        ship.linkProduct(product);
    }

    @Override
    public void unlinkProduct(String productId, String shipId) throws ProductNotFoundException, ShipNotFoundException, ProductNotInMenuException {
        Product product = products.get(productId);
        // Si el producto no existe, lanzamos la excepción
        if (product == null)
            throw new ProductNotFoundException();

        Ship ship = getShip(shipId);
        // Si el buque no existe, lanzamos la excepción
        if (ship == null)
            throw new ShipNotFoundException();

        // Comprobamos si el producto está asignado en el buque
        Traversal<Product> positions = ship.linkedProductsPositions();
        while (positions.hasNext()) {
            Position<Product> pos = positions.next();

            if (pos.getElem().getId().equals(productId)) {
                ship.deleteLinkedProduct(pos);
                return;
            }
        }

        // Si no se encuentra el producto, lanzamos la excepción
        throw new ProductNotInMenuException();
    }

    @Override
    public Iterator<Product> getVoyageProductsByCategory(String voyageId, String categoryId) throws VoyageNotFoundException, CategoryNotFoundException, NoProductsException {
        Voyage voyage = getVoyage(voyageId);
        // Si la travesía no existe, lanzamos la excepción
        if (voyage == null)
            throw new VoyageNotFoundException();

        Category category = getCategory(categoryId);
        // Si la categoría no existe, lanzamos la excepción
        if (category == null)
            throw new CategoryNotFoundException();

        // Si la categoría no tiene productos, lanzamos la excepción
        if (category.numProducts() == 0)
            throw new NoProductsException();

        List<Product> products = new LinkedList<>();
        Iterator<Product> iterator = voyage.getShip().linkedProducts();
        while (iterator.hasNext()) {
            Product product = iterator.next();
            // Si el producto es de la categoría deseada, lo añadimos a la lista encadenada
            if (product.getCategory().getId().equals(categoryId))
                products.insertEnd(product);
        }

        // Si la travesía no tiene productos de esa categoría, lanzamos la excepción
        if (products.isEmpty())
            throw new NoProductsException();

        // Devolvemos el iterador
        return products.values();
    }

    @Override
    public void makeOrder(String clientId, String voyageId, String[] products, double price) throws ClientNotFoundException, VoyageNotFoundException, ProductNotFoundException, ProductNotInMenuException, ClientIsNotInVoyageException {
        Client client = getClient(clientId);
        // Si el cliente no existe, lanzamos la excepción
        if (client == null)
            throw new ClientNotFoundException();

        Voyage voyage = getVoyage(voyageId);
        // Si la travesía no existe, lanzamos la excepción
        if (voyage == null)
            throw new VoyageNotFoundException();

        boolean productExists = productsInIterator(products, this.products.values());
        // Si algún producto no existe, lanzamos la excepción
        if (!productExists)
            throw new ProductNotFoundException();

        boolean productInMenu = productsInIterator(products, voyage.getShip().linkedProducts());
        // Si algún producto no está en el menú del buque, lanzamos la excepción
        if (!productInMenu)
            throw new ProductNotInMenuException();

        boolean clientInReserv = false;
        Iterator<Reservation> iterator = voyage.reservations();
        while (iterator.hasNext()) {
            Reservation reserv = iterator.next();

            Iterator<Client> itClients = reserv.clients();
            // Comprobamos si el cliente pertenece a la travesía
            while (itClients.hasNext()) {
                Client rsvClient = itClients.next();
                if (rsvClient.getId().equals(clientId)) {
                    clientInReserv = true;
                    break;
                }
            }
            if (clientInReserv)
                break;
        }

        // Si el cliente no está en la travesía, lanzamos la excepción
        if (!clientInReserv)
            throw new ClientIsNotInVoyageException();

        LinkedList<Product> productList = (LinkedList<Product>) getProductList(products, voyage.getShip().linkedProducts());

        // Añadimos el pedido
        Order order = new Order(orderNumber, client, voyage, productList, price);
        client.addOrder(order);
        voyage.addOrder(order);
        // Actualizamos el vector ordenado de clientes con más pedidos
        updateBest5Clients(client);
        orderNumber++;
    }

    @Override
    public Order serveOrder(String voyageId) throws VoyageNotFoundException, NoOrdersException {
        Voyage voyage = getVoyage(voyageId);
        // Si la travesía no existe, lanzamos la excepción
        if (voyage == null)
            throw new VoyageNotFoundException();

        // Si la travesía no tiene pedidos por servir, lanzamos la excepción
        if (voyage.numPendingOrders() == 0)
            throw new NoOrdersException();

        return voyage.serveOrder();
    }

    @Override
    public Iterator<Order> getOrdersByClient(String clientId) throws ClientNotFoundException, NoOrdersException {
        Client client = getClient(clientId);
        // Si el cliente no existe, lanzamos la excepción
        if (client == null)
            throw new ClientNotFoundException();

        // Si el cliente no tiene pedidos, lanzamos la excepción
        if (client.numOrders() == 0)
            throw new NoOrdersException();

        return client.orders();
    }

    @Override
    public Iterator<Order> getOrdersByShip(String shipId) throws ShipNotFoundException, NoOrdersException {
        Ship ship = getShip(shipId);
        // Si el buque no existe, lanzamos la excepción
        if (ship == null)
            throw new ShipNotFoundException();

        List<Order> shipOrders = new LinkedList<>();
        Iterator<Voyage> iterator = getVoyages().values();
        while (iterator.hasNext()) {
            Voyage voyage = iterator.next();
            // Si la travesía la realiza el buque indicado, añadimos los pedidos
            if (voyage.getShip().getId().equals(shipId)) {
                // Obtenemos los pedidos de la travesía
                Iterator<Order> itOrders = voyage.orders();
                // Las añadimos a la lista
                while (itOrders.hasNext())
                    shipOrders.insertEnd(itOrders.next());
            }
        }

        // Si no hay pedidos en ninguna de las travesías del buque, lanzamos la excepción
        if (shipOrders.isEmpty())
            throw new NoOrdersException();

        return shipOrders.values();
    }

    @Override
    public LoyaltyLevel getLevel(String clientId) throws ClientNotFoundException {
        Client client = getClient(clientId);
        // Si el cliente no existe, lanzamos la excepción
        if (client == null)
            throw new ClientNotFoundException();

        return client.getLevel();
    }

    @Override
    public Iterator<Route> getRoutesByOrigin(String portId) throws NoRouteException {
        // Si no hay ningún trayecto, lanzamos la excepción
        if (numRoutes() == 0)
            throw new NoRouteException();

        List<Route> routes = new LinkedList<>();
        Iterator<Route> iterator = getRoutes().values();
        while (iterator.hasNext()) {
            Route route = iterator.next();
            // Si el puerto de origen es el que se busca, lo añadimos a la lista de trayectos
            if (route.getBeginningPort().equals(portId))
                routes.insertEnd(route);
        }

        // Si no hay ningún trayecto con el puerto de origen especificado, lanzamos la excepción
        if (routes.isEmpty())
            throw new NoRouteException();

        return routes.values();
    }

    @Override
    public Route getRoute(String idBeginningPort, String idArrivalPort) throws NoRouteException, SamePortException {
        // Si el puerto de origen y destino son los mismos, lanzamos la excepción
        if (idBeginningPort.equals(idArrivalPort))
            throw new SamePortException();

        Iterator<Route> iterator = getRoutes().values();
        while (iterator.hasNext()) {
            Route route = iterator.next();
            // Si encontramos la travesía con el puerto de origen y destino que buscamos, la devolvemos
            if (route.getBeginningPort().equals(idBeginningPort) && route.getArrivalPort().equals(idArrivalPort))
                return route;
        }

        // Si no se ha encontrado la travesía, lanzamos la excepción
        throw new NoRouteException();
    }

    @Override
    public Iterator<Voyage> getVoyagesByRoute(String routeId) throws NoVoyagesException {
        // Si no hay travesías, lanzamos la excepción
        if (numVoyages() == 0)
            throw new NoVoyagesException();

        List<Voyage> voyages = new LinkedList<>();
        Iterator<Voyage> iterator = getVoyages().values();
        while (iterator.hasNext()) {
            Voyage voyage = iterator.next();
            // Si la travesía es del trayecto especificado la añadimos a la lista
            if (voyage.getRoute().getId().equals(routeId))
                voyages.insertEnd(voyage);
        }

        // Si no hay travesías del trayecto indicado, lanzamos la excepción
        if (voyages.isEmpty())
            throw new NoVoyagesException();

        return voyages.values();
    }

    @Override
    public Iterator<Client> best5Clients() throws NoClientException {
        // Si no hay clientes, lanzamos la excepción
        if (numClients() == 0)
            throw new NoClientException();

        // Si no ningún cliente en este vector ordenado es que no hay pedidos, entonces lanzamos la excepción
        if (best5Clients.isEmpty())
            throw new NoClientException();

        int count = 0;
        List<Client> best5 = new LinkedList<>();
        Iterator<Client> iteratorCl = best5Clients.values();
        while (iteratorCl.hasNext()) {
            // Solo añadimos los 5 mejores, es decir, los 5 primeros del vector ordenado
            if (count < 5) {
                best5.insertEnd(iteratorCl.next());
                count++;
            }
            else
                break;
        }

        return best5.values();
    }

    @Override
    public boolean existsRouteBetween(String idAPort, String idBPort) throws SamePortException, SrcPortNotFoundException, DstPortNotFoundException {
        // Si el puerto A y el B son los mismos, lanzamos la excepción
        if (idAPort.equals(idBPort))
            throw new SamePortException();

        boolean[] portsExist = checkIfPortsExist(idAPort, idBPort);
        // Si el puerto A no existe, lanzamos la excepción
        if (!portsExist[0])
            throw new SrcPortNotFoundException();
        // Si el puerto B no existe, lanzamos la excepción
        if (!portsExist[1])
            throw new DstPortNotFoundException();

        DirectedGraphImpl<String, Route> directedGraph = createDirectedGraph();
        // Array donde se guardarán los puertos visitados, primero se guarda el puerto de origen
        ArrayList<String> visitedPorts = new ArrayList<>();
        visitedPorts.add(idAPort);

        // Devolvemos el resultado del método recursivo
        return visitPort(idAPort, idBPort, visitedPorts, directedGraph);
    }

    @Override
    public Iterator<Route> getBestKmsRoute(String idAPort, String idBPort) throws SamePortException, SrcPortNotFoundException, DstPortNotFoundException, NoRouteException {
        // Si el puerto A y el B son los mismos, lanzamos la excepción
        if (idAPort.equals(idBPort))
            throw new SamePortException();

        boolean[] portsExist = checkIfPortsExist(idAPort, idBPort);
        // Si el puerto A no existe, lanzamos la excepción
        if (!portsExist[0])
            throw new SrcPortNotFoundException();
        // Si el puerto B no existe, lanzamos la excepción
        if (!portsExist[1])
            throw new DstPortNotFoundException();

        DirectedGraphImpl<String, Route> directedGraph = createDirectedGraph();
        ArrayList<String> visitedPorts = new ArrayList<>();
        visitedPorts.add(idAPort);
        // Si no existe ningún camino entre A y B, lanzamos la excepción
        if (!visitPort(idAPort, idBPort, visitedPorts, directedGraph))
            throw new NoRouteException();

        // Inicializamos el HashMap de las distancias con todos los vértices (id del puerto)
        Map<String, Double> distTo = new HashMap<>();
        Iterator<Vertex<String>> itVertexs = directedGraph.vertexs();
        while (itVertexs.hasNext())
            distTo.put(itVertexs.next().getValue(), 0.0);

        String srcPort = idAPort;
        ArrayList<DirectedEdge<Route, String>> settledEdges = new ArrayList<>();
        ArrayList<DirectedEdge<Route, String>> unsettledEdges = new ArrayList<>();
        boolean end = false;
        while (!end) {
            Iterator<Edge<Route, String>> iterator = directedGraph.edgesWithSource(directedGraph.getVertex(srcPort));
            // Añadimos las aristas a la lista de aristas no visitadas
            addUnsettledEdges(unsettledEdges, iterator);

            // Actualizamos la distancia
            DirectedEdge<Route, String> lowestDistanceEdge = getLowestDistanceEdge(unsettledEdges, distTo);
            Route route = lowestDistanceEdge.getLabel();
            double newDistance = distTo.get(route.getBeginningPort()) + route.getKms();
            distTo.put(route.getArrivalPort(), newDistance);

            // Eliminamos la arista de la lista de aristas no visitadas
            unsettledEdges.remove(lowestDistanceEdge);
            settledEdges.add(lowestDistanceEdge);

            // Si el puerto de destino es el puerto B, se termina el while
            if (route.getArrivalPort().equals(idBPort))
                end = true;
            else
                srcPort = route.getArrivalPort();
        }

        // En la lista de aristas visitadas empezamos de atrás hacia delante
        LinkedList<Route> bestRoute = new LinkedList<>();
        int length = settledEdges.size();
        for (int i = length - 1; i >= 0; i--) {
            Route route = settledEdges.get(i).getLabel();

            // Si es la última (la primera iteración) de la lista esta arista pertenece al trayecto
            if (i == length - 1) {
                srcPort = route.getBeginningPort();
                bestRoute.insertBeginning(route);
            }
            else {
                // Si el puerto de destino es el anterior puerto de origen esta arista pertenece al trayecto
                if (route.getArrivalPort().equals(srcPort)) {
                    srcPort = route.getBeginningPort();
                    // Lo insertamos al principio de la lista ya que la primera iteración empieza desde el destino
                    bestRoute.insertBeginning(route);
                }
            }

            // Lo eliminamos de la lista
            settledEdges.remove(i);
        }

        return bestRoute.values();
    }

    @Override
    public Iterator<Route> getBestPortsRoute(String idAPort, String idBPort) throws SamePortException, SrcPortNotFoundException, DstPortNotFoundException, NoRouteException {
        // Si el puerto A y el B son los mismos, lanzamos la excepción
        if (idAPort.equals(idBPort))
            throw new SamePortException();

        boolean[] portsExist = checkIfPortsExist(idAPort, idBPort);
        // Si el puerto A no existe, lanzamos la excepción
        if (!portsExist[0])
            throw new SrcPortNotFoundException();
        // Si el puerto B no existe, lanzamos la excepción
        if (!portsExist[1])
            throw new DstPortNotFoundException();

        DirectedGraphImpl<String, Route> directedGraph = createDirectedGraph();
        ArrayList<String> visitedPorts = new ArrayList<>();
        visitedPorts.add(idAPort);
        // Si no existe ningún camino entre A y B, lanzamos la excepción
        if (!visitPort(idAPort, idBPort, visitedPorts, directedGraph))
            throw new NoRouteException();

        Map<Route, Route> parents = new HashMap<>();
        ArrayList<DirectedEdge<Route, String>> temp = new ArrayList<>();

        // Añadimos las posibles aristas desde el origen al HashMap y a la lista temporal
        Iterator<Edge<Route, String>> iterator = directedGraph.edgesWithSource(directedGraph.getVertex(idAPort));
        while (iterator.hasNext()) {
            DirectedEdge<Route, String> edge = (DirectedEdge<Route, String>) iterator.next();
            temp.add(edge);
            parents.put(edge.getLabel(), null);
        }

        // Vamos iterando hasta que la lista temporal de aristas este vacía
        Route parent = null;
        while (!temp.isEmpty()) {
            // Seleccionamos el primer valor de la lista
            parent = temp.get(0).getLabel();
            String srcPort = temp.get(0).getVertexDst().getValue();
            // Vamos iterando las posibles aristas desde el vértice
            iterator = directedGraph.edgesWithSource(directedGraph.getVertex(srcPort));
            while (iterator.hasNext()) {
                DirectedEdge<Route, String> edge = (DirectedEdge<Route, String>) iterator.next();
                Route route = edge.getLabel();
                // Si ya se ha visitado, pasamos a la siguiente iteración
                boolean visited = parents.containsKey(route);
                if (visited)
                    continue;
                else {
                    temp.add(edge);
                    parents.put(route, parent);

                    // Si el puerto de destino del trayecto es el que buscamos, se termina el while y se devuelve el camino (lista de trayectos)
                    if (route.getArrivalPort().equals(idBPort))
                        return getPath(parents, route).values();
                }
            }
            // Eliminamos el primer elemento de la lista
            temp.remove(0);
        }

        // Aquí no se debería llegar, ya que se comprueba si existe un camino entre el puerto A y el B
        return null;
    }

    @Override
    public int numPorts() {
        return ports.size();
    }

    @Override
    public int numCategories() {
        return categories.size();
    }

    @Override
    public int numProducts() {
        return products.size();
    }

    @Override
    public int numProducts(String categoryId) {
        int numProducts = 0;

        Iterator<Product> iterator = products.values();
        // Recorremos todos los productos y filtramos por el categoryId
        while (iterator.hasNext()) {
            Product product = iterator.next();
            if (product.getCategory().getId().equals(categoryId))
                numProducts++;
        }

        return numProducts;
    }

    @Override
    public int numProductsByShip(String shipId) {
        Ship ship = getShip(shipId);
        return ship.numLinkedProducts();
    }

    @Override
    public int numOrders(String voyageId) {
        Voyage voyage = getVoyage(voyageId);
        if (voyage != null)
            return voyage.numPendingOrders() + voyage.numServedOrders();
        else
            return -1;
    }

    @Override
    public Category getCategory(String id) {
        Iterator<Category> iterator = categories.values();
        // Recorremos todas las categorías y si la encontramos la devolvemos
        while (iterator.hasNext()) {
            Category category = iterator.next();
            if (category.getId().equals(id))
                return category;
        }

        return null;
    }

    // Métodos privados (solo se utilizan en esta clase)

    /**
     * Se utiliza para comprobar si el producto se encuentra en el iterador
     * @param products El vector con los ids de los productos
     * @param it El iterador con todos los productos
     * @return true si el producto se encuentra en el iterador, false en caso contrario
     */
    private boolean productsInIterator(String[] products, Iterator<Product> it) {
        for (String productId : products) {
            boolean productExists = false;

            // Comprobamos si el producto está en el iterador
            while (it.hasNext()) {
                Product product = it.next();
                if (product.getId().equals(productId)) {
                    productExists = true;
                    break;
                }
            }

            if (!productExists)
                return false;
        }

        return true;
    }

    /**
     * Este método se utiliza para obtener los productos de un pedido buscándolos en la lista de productos
     * @param products El vector con los ids de los productos que queremos
     * @param it El iterador con todos los productos
     * @return La lista de productos que queremos
     */
    private List<Product> getProductList(String[] products, Iterator<Product> it) {
        List<Product> productList = new LinkedList<>();
        for (String productId : products) {
            while (it.hasNext()) {
                Product product = it.next();
                // Si está en la lista de productos, lo añadimos a lista de productos del pedido
                if (product.getId().equals(productId)) {
                    productList.insertEnd(product);
                    break;
                }
            }
        }
        return productList;
    }

    private void updateBest5Clients(Client client) {
        best5Clients.delete(client);
        best5Clients.update(client);
    }

    /**
     * Método para comprobar si el puerto A y el puerto B existen
     * @param idAPort Id del puerto A
     * @param idBPort Id del puerto B
     * @return Devolvemos un array de booleanos con dos posiciones, indicando si los puertos existen
     */
    private boolean[] checkIfPortsExist(String idAPort, String idBPort) {
        boolean portAExists = false, portBExists = false;
        Iterator<Port> iterator = ports.values();
        while (iterator.hasNext()) {
            Port port = iterator.next();
            // Comprobamos si los puertos A y B existen
            if (port.getId().equals(idAPort))
                portAExists = true;
            else if (port.getId().equals(idBPort))
                portBExists = true;

            // Si existen rompemos el while
            if (portAExists && portBExists)
                break;
        }

        // Devolvemos array con un booleano indicando si existe el puerto A en la primera posición y en el caso del puerto B en la segunda posición
        return new boolean[]{portAExists, portBExists};
    }

    /**
     * Crea el grafo dirigido con los trayectos
     * @return Devuelve el grafo dirigido
     */
    private DirectedGraphImpl<String, Route> createDirectedGraph() {
        // Creamos el grafo dirigido
        DirectedGraphImpl<String, Route> directedGraph = new DirectedGraphImpl<>();

        Iterator<Route> iterator = getRoutes().values();
        while (iterator.hasNext()) {
            Route route = iterator.next();

            // Comprobamos si los vértices existen
            Vertex<String> va = directedGraph.getVertex(route.getBeginningPort());
            Vertex<String> vb = directedGraph.getVertex(route.getArrivalPort());

            // Si no existen, creamos los vértices
            if (va == null)
                va = directedGraph.newVertex(route.getBeginningPort());
            if (vb == null)
                vb = directedGraph.newVertex(route.getArrivalPort());

            Edge<Route, String> edge = directedGraph.getEdge(va, vb);
            // Si no existe la arista, la creamos
            if (edge == null) {
                edge = directedGraph.newEdge(va, vb);
                edge.setLabel(route);
            }
        }

        return directedGraph;
    }

    /**
     * Método recursivo para ir visitando los puertos desde el origen hasta intentar encontrar el destino
     * @param idSrcPort El id del puerto de origen
     * @param idDstPort El id del puerto de destino
     * @param visitedPorts El array que contiene los puertos que ya se han visitado, para no volver a pasar por donde ya se ha pasado
     * @param directedGraph El grafo dirigido
     * @return true si el puerto B es el puerto de destino, false si no se encuentra
     */
    private boolean visitPort(String idSrcPort, String idDstPort, ArrayList<String> visitedPorts, DirectedGraphImpl<String, Route> directedGraph) {
        Iterator<Edge<Route, String>> srcEdges = directedGraph.edgesWithSource(directedGraph.getVertex(idSrcPort));
        while (srcEdges.hasNext()) {
            DirectedEdge<Route, String> edge = (DirectedEdge<Route, String>) srcEdges.next();
            String idBPort = edge.getVertexDst().getValue();
            if (idBPort.equals(idDstPort))
                return true;
            else {
                if (!visitedPorts.contains(idBPort)) {
                    visitedPorts.add(idBPort);
                    boolean exists = visitPort(idBPort, idDstPort, visitedPorts, directedGraph);
                    if (exists)
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * Método para añadir las aristas no visitadas en la lista
     * @param unsettledEdges Lista con las aristas no visitadas
     * @param iterator El iterador con las aristas posibles desde un vértice
     */
    private void addUnsettledEdges(ArrayList<DirectedEdge<Route, String>> unsettledEdges, Iterator<Edge<Route, String>> iterator) {
        while (iterator.hasNext()) {
            DirectedEdge<Route, String> edge = (DirectedEdge<Route, String>) iterator.next();
            // Si la arista no está en la lista, la añadimos
            if (!unsettledEdges.contains(edge))
                unsettledEdges.add(edge);
        }
    }

    /**
     * Método para obtener la arista con menor distancia entre las aristas no visitadas
     * @param unsettledEdges Lista de aristas no visitadas
     * @param distTo HashMap con las distancias hacia los vértices
     * @return La arista con menor distancia
     */
    private DirectedEdge<Route, String> getLowestDistanceEdge(ArrayList<DirectedEdge<Route, String>> unsettledEdges, Map<String, Double> distTo) {
        DirectedEdge<Route, String>  lowestDistanceEdge = null;
        double lowestDistance = Double.MAX_VALUE;
        // Recorremos las aristas que no han sido visitadas
        for (DirectedEdge<Route, String> edge : unsettledEdges) {
            Route route = edge.getLabel();
            // Sumamos la distancia desde el nodo inicial (es lo que se guarda en el HashMap) más la distancia de la arista
            double newDistance = distTo.get(route.getBeginningPort()) + route.getKms();
            // Si la distancia es menor que la que está en la variable, guardaremos la distancia en la variable y la arista
            if (newDistance < lowestDistance) {
                lowestDistanceEdge = edge;
                lowestDistance = newDistance;
            }
        }

        return lowestDistanceEdge;
    }

    /**
     * Método para obtener el camino entre el puerto de origen y el puerto de destino
     * @param parents El HashMap con los trayectos padre
     * @param lastRoute El último trayecto del camino
     * @return Un iterador con el camino
     */
    private List<Route> getPath(Map<Route, Route> parents, Route lastRoute) {
        List<Route> path = new LinkedList<>();
        // Añadimos el último trayecto a la lista
        path.insertBeginning(lastRoute);

        // Obtenemos el trayecto padre del último trayecto
        Route route = parents.get(lastRoute);

        // Con este while vamos obteniendo el camino
        while (route != null) {
            // Añadimos el trayecto al principio de la lista, ya que empezamos desde el final del camino
            path.insertBeginning(route);
            // Asignamos su padre a la variable
            route = parents.get(route);
        }
        return path;
    }
}
