/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import rental.CarRentalCompany;

/**
 *
 * @author Bavo Goosens & Michiel Vandendriessche
 */
public class Finder {
    
    @PersistenceContext
    private EntityManager em;
    
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
    
    public void persistEntity(Object entity) {
        this.em.persist(entity);
    }
    
    public void flush() {
        this.em.flush();
    }
    
}
