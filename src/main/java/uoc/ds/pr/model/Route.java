package uoc.ds.pr.model;

import edu.uoc.ds.adt.helpers.Position;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.adt.sequential.List;
import edu.uoc.ds.traversal.Traversal;

import java.util.Comparator;

public class Route implements Comparable<Route> {
    public static final Comparator<Route> CMP_V = (r1, r2) -> Integer.compare(r1.voyages.size(), r2.voyages.size());
    private String id;
    private String beginningPort;
    private String arrivalPort;
    private double kms;
    private Port srcPort;
    private Port dstPort;
    List<Voyage> voyages;


    public Route(String id, Port srcPort, Port dstPort, double kms) {
        this.setId(id);
        this.setBeginningPort(srcPort.getId());
        this.setArrivalPort(dstPort.getId());
        this.setKms(kms);
        this.srcPort = srcPort;
        this.dstPort = dstPort;

        voyages = new LinkedList<>();
    }


    public void update(String name, String description) {
        this.setBeginningPort(name);
        this.setArrivalPort(description);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBeginningPort() {
        return beginningPort;
    }

    public void setBeginningPort(String beginningPort) {
        this.beginningPort = beginningPort;
    }

    public String getArrivalPort() {
        return arrivalPort;
    }

    public void setArrivalPort(String arrivalPort) {
        this.arrivalPort = arrivalPort;
    }

    public double getKms() {
        return kms;
    }

    public void setKms(double kms) {
        this.kms = kms;
    }

    @Override
    public int compareTo(Route o) {
        return this.id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return this.getBeginningPort()+"-"+this.getArrivalPort();
    }

    public void addVoyage(Voyage voyage) {
        voyages.insertEnd(voyage);
    }

    public int numVoyages() {
        return voyages.size();
    }

    public void remove(Voyage voyage) {
        Traversal<Voyage> traversal = voyages.positions();
        Position<Voyage> p = null;
        while (traversal.hasNext()) {
            p = traversal.next();
            if (p.getElem().getId().equals(voyage.getId())) {
                voyages.delete(p);
            }
        }
    }

    public Port getSrcPort() {
        return srcPort;
    }

    public Port getDstPort() {
        return dstPort;
    }
}
