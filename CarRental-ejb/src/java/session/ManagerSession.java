package session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;

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
        try {
            return this.getCompany(company).getCar(id).getReservations().size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Set<Reservation> out = new HashSet<Reservation>();
        try {
            for(Car c: this.getCompany(company).getCars(type)){
                out.addAll(c.getReservations());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return out.size();
    }

    @Override
    public int getNumberOfReservationsBy(String renter) {
        Set<Reservation> out = new HashSet<Reservation>();
        for(CarRentalCompany crc : this.getRentals().values()) {
            out.addAll(crc.getReservationsBy(renter));
        }
        return out.size();
    }

    @Override
    public void addCarRentalCompany(String name) {
        CarRentalCompany company = new CarRentalCompany(name);
        em.persist(company);
    }

    @Override
    public void addCarType(String carRentalCompany, String typeName, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed) {
        CarRentalCompany company = this.getCompany(carRentalCompany);
        company.addCarType(new CarType(typeName, nbOfSeats, trunkSpace, rentalPricePerDay, smokingAllowed));
        em.flush();
    }
    
    public Map<String, CarRentalCompany> getRentals() {
        Map<String, CarRentalCompany> companies = new HashMap<String, CarRentalCompany>();
        Query query = em.createQuery("SELECT c FROM CarRentalCompany c");
        for (Iterator it = query.getResultList().iterator(); it.hasNext();) {
            CarRentalCompany company = (CarRentalCompany) it.next();
            companies.put(company.getName(), company);
        }
        return companies;
    }
    
    public CarRentalCompany getCompany(String companyName) {
        return this.em.find(CarRentalCompany.class, companyName);
    }
}