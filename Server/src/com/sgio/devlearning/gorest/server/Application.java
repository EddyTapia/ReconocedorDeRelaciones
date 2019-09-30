/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.server;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author sgioh
 */
public class Application {
    
    public static void main(String[] args) throws Exception {
        Path configurationPath = Paths.get(System.getProperty("user.dir"), "settings.json");
        Configuration configuration = getConfigurationFromFile(configurationPath.toString());
        
        TcpServer server = new TcpServer(configuration);
        
        Scanner sc = new Scanner(System.in);
        while(true) {
            String command = sc.nextLine().trim().toLowerCase();
            
            if (command.equals("close")) {
                server.close();
                System.out.println("Exit with code 0");
                
                break;
            }
        }
    }
    
    public static Configuration getConfigurationFromFile(String filePath) 
            throws FileNotFoundException, IOException {
        try (JsonReader reader = new JsonReader(new FileReader(filePath))) {
            return new Gson().fromJson(reader, Configuration.class);
        }
    }
}
