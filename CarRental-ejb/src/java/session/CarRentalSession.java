package session;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();
    private EntityManager em;

    @Override
    public Set<String> getAllRentalCompanies() {
        return new HashSet<String>(this.getRentals().keySet());
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarType> availableCarTypes = new LinkedList<CarType>();
        for(String crc : getAllRentalCompanies()) {
            for(CarType ct : this.getRentals().get(crc).getAvailableCarTypes(start, end)) {
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            }
        }
        return availableCarTypes;
    }

    @Override
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        try {
            Quote out = this.getCompany(company).createQuote(constraints, renter);
            quotes.add(out);
            return out;
        } catch(Exception e) {
            throw new ReservationException(e);
        }
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                done.add(this.getCompany(quote.getRentalCompany()).confirmQuote(quote));
            }
        } catch (Exception e) {
            for(Reservation r:done)
                this.getCompany(r.getRentalCompany()).cancelReservation(r);
            throw new ReservationException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
    
    public CarRentalCompany getCompany(String companyName) {
        return this.em.find(CarRentalCompany.class, companyName);
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
    
    
}