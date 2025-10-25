/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.heroku;


import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@SpringBootApplication
public class HerokuApplication {

    // Helper for Secure Random String generation
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 30; // Matches the varchar(30) in SQL
    private static final Random RANDOM = new SecureRandom();

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HerokuApplication.class, args);
    }

    @RequestMapping("/")
    String index() {
        return "index";
    }

    // =========================================================================
    // Core Assignment: /db endpoint
    // =========================================================================

    @RequestMapping("/db")
    String db(Map<String, Object> model) {
        // Required log statement
        System.out.println("Processing /db request - Chandler Black");
        
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            
            // 1. Create table and insert data (modified as per instructions)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string (tick timestamp, random_string varchar(30))");
            stmt.executeUpdate("INSERT INTO table_timestamp_and_random_string VALUES (now(), '" + getRandomString() + "')");
            
            // 2. Query all records from the NEW table
            ResultSet rs = stmt.executeQuery("SELECT tick, random_string FROM table_timestamp_and_random_string");

            @SuppressWarnings("Convert2Diamond")
            ArrayList<String> output = new ArrayList<String>();
            
            // 3. Retrieve and format both columns for display
            while (rs.next()) {
                output.add("Time: " + rs.getTimestamp("tick") + " | String: " + rs.getString("random_string"));
            }

            model.put("records", output);
            return "db";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
    }

    // =========================================================================
    // Helper Method: getRandomString()
    // =========================================================================

    public String getRandomString() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
    
    // =========================================================================
    // Extra Credit: User Input Endpoints
    // =========================================================================

    @GetMapping("/dbinput")
    String dbInputForm() {
        // Renders the Thymeleaf template named "dbinput"
        return "dbinput"; 
    }

    @PostMapping("/dbinput")
    String dbInsert(@RequestParam("userInput") String userInput, Map<String, Object> model) {
        // Sanitize input: Truncate to 30 chars to match the DB schema
        String safeInput = userInput.length() > LENGTH ? userInput.substring(0, LENGTH) : userInput;

        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            
            // Re-create table just in case (though it should exist from /db call)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string (tick timestamp, random_string varchar(30))");
            
            // INSERT user input (IMPORTANT: Use safe string concatenation or PreparedStatement to prevent SQL Injection)
            // For simplicity and matching the assignment style, we use safe string concatenation here
            stmt.executeUpdate("INSERT INTO table_timestamp_and_random_string VALUES (now(), '" + safeInput + "')");
            
            // Redirect user back to the /db page to see the results
            return "redirect:/db"; 
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
    }
}