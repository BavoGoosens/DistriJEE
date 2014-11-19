package session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        try {
            return new HashSet<CarType>(this.getCompany(company).getAllTypes());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            for(Car c: this.getCompany(company).getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        Query query = em.createQuery("SELECT	COUNT(r) "
                + "FROM	CarRentalCompany crc JOIN crc.cars c JOIN c.reservations r "
                + "WHERE crc.name = :name AND  c.type.name = :type AND c.id = :id");
        query.setParameter("name", company);
        query.setParameter("type", type);
        query.setParameter("id", id);
        int nb = query.getFirstResult();
        return nb;
        /*try {
            return this.getCompany(company).getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }*/
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Query query = em.createQuery( "SELECT	COUNT(r) "
                + "FROM	CarRentalCompany crc JOIN crc.cars c JOIN c.reservations r "
                + "WHERE crc.name = :name AND  c.type.name = :type");
        query.setParameter("name", company);
        query.setParameter("type", type);
        int nb  = query.getFirstResult();
        return nb;
        /*Set<Reservation> out = new HashSet<Reservation>();
        try {
            for(Car c: this.getCompany(company).getCars(type)){
                out.addAll(c.getReservations());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return out.size();*/
    }

    @Override
    public int getNumberOfReservationsBy(String renter) {
        
        Query query = em.createQuery( "SELECT	COUNT(r) "
                + "FROM	Reservation r WHERE r.carRenter = :renter");
        query.setParameter("renter", renter);
        int nb  = query.getFirstResult();
        return nb;
        
        /*Set<Reservation> out = new HashSet<Reservation>();
        for(CarRentalCompany crc : this.getRentals().values()) {
            out.addAll(crc.getReservationsBy(renter));
        }
        return out.size();*/
    }

    @Override
    public void addCarRentalCompany(String name) {
        CarRentalCompany company = new CarRentalCompany(name);
        em.persist(company);
    }

    @Override
    public void addCarType(  String carRentalCompany, String typeName, 
                             int nbOfSeats, float trunkSpace, 
                             double rentalPricePerDay, boolean smokingAllowed) {
        /*CarType type = new CarType(typeName, nbOfSeats, trunkSpace, rentalPricePerDay, smokingAllowed);
        for (CarRentalCompany company: this.getRentals().values()) {
            if (company.getAllTypes().contains(type)) {
                type = company.getType(typeName);
                break;
            }
        }*/
        CarType type;
        Query query = em.createQuery("SELECT c FROM CarType c WHERE c.name = :typeName");
        query.setParameter("typeName", typeName);
        List<CarType> carTypes = query.getResultList();
        if (carTypes.isEmpty()) {
            type = new CarType(typeName, nbOfSeats, trunkSpace, rentalPricePerDay, smokingAllowed);
        } else {
            type = carTypes.get(0);
        }
        
        CarRentalCompany company = this.getCompany(carRentalCompany);
        company.addCarType(type);
        em.flush();
    }
    
    @Override
    public void addCar(String carRentalCompany, String carTypeName) {
        CarRentalCompany company = this.getCompany(carRentalCompany);
        CarType type = company.getType(carTypeName);
        Car car = new Car(type);
        company.addCar(car);
        em.flush();
    }
    
    @Override
    public Collection<String> getAllCompanies() {
        return this.getRentals().keySet();
    }
    
    @Override
    public Collection<String> getAllCarTypesForCompany(String companyName) {
        Query query = em.createQuery(
                  "SELECT crc.carTypes "
                + "FROM CarRentalCompany crc "
                + "WHERE crc.name = :name");
        query.setParameter("name", companyName);
        List<CarType> types = query.getResultList();
        List<String>  names = new ArrayList<String>();
        for (CarType type: types) {
            names.add(type.getName());
        }
        return names;
    }
    
    @Override
    public Set<String> getBestClients() {
        Query query = em.createQuery(""
                + "SELECT   r.carRenter, COUNT(r) "
                + "FROM     CarRentalCompany crc JOIN crc.reservations r");
        List<Object[]> result = query.getResultList();
        Set<String> bestClients = new HashSet<String>();
        int bestReservations = 0;
        for (Object[] carRenter: result) {
            String renterName = (String) carRenter[0];
            int reservations = (Integer) carRenter[1];
            if (reservations > bestReservations) {
                bestClients.clear();
                bestClients.add(renterName);
                bestReservations = reservations;
            } else if (reservations == bestReservations) {
                bestClients.add(renterName);
            }
        }
        return bestClients;
    }
    
    @Override
    public CarType getMostPopularCarTypeIn(String companyName) {
        Query query = em.createQuery(""
                + "SELECT   c.type, MAX( COUNT(r) ) "
                + "FROM     CarRentalCompany crc JOIN crc.reservations r "
                + "                             JOIN Car c ON r.carId = c.id "
                + "WHERE    crc.name = :companyName "
                + "GROUP BY c.type");
        query.setParameter("companyName", companyName);
        List<Object[]> result = query.getResultList();
        return (CarType) result.get(0)[0];
    }
    
    private Map<String, CarRentalCompany> getRentals() {
        Map<String, CarRentalCompany> companies = new HashMap<String, CarRentalCompany>();
        Query query = em.createQuery("SELECT c FROM CarRentalCompany c");
        for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
            CarRentalCompany company = (CarRentalCompany) it.next();
            companies.put(company.getName(), company);
        }
        return companies;
    }
    
    private CarRentalCompany getCompany(String companyName) {
        return this.em.find(CarRentalCompany.class, companyName);
    }
    
}