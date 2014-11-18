/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import rental.CarType;
import session.ManagerSessionRemote;

/**
 *
 * @author parallels
 */
public class ManagerClient {
    
    public void run() throws IOException, NamingException {
        ManagerSessionRemote ms = 
                (ManagerSessionRemote) new InitialContext().lookup(ManagerSessionRemote.class.getName());
        ManagerClient.loadCompany("Dockx", "dockx.csv", ms);
        ManagerClient.loadCompany("Hertz", "hertz.csv", ms);
        Collection<String> types = ms.getAllCarTypesForCompany("Dockx");
        for (String type: types) {
            System.out.println(type);
        }
    }

    public static void loadCompany(String companyName, String datafile, ManagerSessionRemote ms)
            throws NumberFormatException, IOException {
        ms.addCarRentalCompany(companyName);
        //open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(ManagerClient.class.getClassLoader().getResourceAsStream(datafile)));
        //while next line exists
        while (in.ready()) {
            //read line
            String line = in.readLine();
            //if comment: skip
            if (line.startsWith("#")) {
                continue;
            }
            //tokenize on ,
            StringTokenizer csvReader = new StringTokenizer(line, ",");
            
            // variables for new car type
            String typeName = csvReader.nextToken();
            int nbOfSeats = Integer.parseInt(csvReader.nextToken());
            float trunkSpace = Float.parseFloat(csvReader.nextToken());
            double rentalPricePerDay = Double.parseDouble(csvReader.nextToken());
            boolean smokingAllowed = Boolean.parseBoolean(csvReader.nextToken());
            
            ms.addCarType(  companyName, 
                            typeName,
                            nbOfSeats,
                            trunkSpace,
                            rentalPricePerDay,
                            smokingAllowed);
            
            // number of cars that need to be added for this type
            int carCount = Integer.parseInt(csvReader.nextToken());
            
            // add cars
            for (int i = 1; i <= carCount; i++) {
                ms.addCar(companyName, typeName);
            }
        }
    }
    
}
