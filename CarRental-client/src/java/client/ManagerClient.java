/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    
    public static void main(String[] args) throws IOException, NamingException {
        ManagerSessionRemote ms = 
                (ManagerSessionRemote) new InitialContext().lookup(ManagerSessionRemote.class.getName());
        ManagerClient.loadCompany("Dockx", "dockx.csv", ms);
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
            
            ms.addCarType(  companyName, 
                            csvReader.nextToken(),
                            Integer.parseInt(csvReader.nextToken()),
                            Float.parseFloat(csvReader.nextToken()),
                            Double.parseDouble(csvReader.nextToken()),
                            Boolean.parseBoolean(csvReader.nextToken()));
        }
    }
    
}
