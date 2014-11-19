package session;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;
import rental.Reservation;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
        
    public Set<String> getBestClients();
    
    public String getCheapestCarType(Date start, Date end);
    
    public int getNumberOfReservations(String company, String type);
      
    public int getNumberOfReservationsBy(String renter);
    
    public void addCarRentalCompany(String name);
    
    public void addCarType(String carRentalCompany, String typeName, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed);
    
    public void addCar(String carRentalCompany, String carTypeName);
    
    public Collection<String> getAllCompanies();
    
    public Collection<String> getAllCarTypesForCompany(String companyName);
    
    public CarType getMostPopularCarTypeIn(String companyName);
}