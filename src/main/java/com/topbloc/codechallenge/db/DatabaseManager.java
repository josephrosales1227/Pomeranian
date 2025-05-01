package com.topbloc.codechallenge.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DatabaseManager {
    private static final String jdbcPrefix = "jdbc:sqlite:";
    private static final String dbName = "challenge.db";
    private static String connectionString;
    private static Connection conn;

    static {
        File dbFile = new File(dbName);
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
    }

    public static void connect() {
        try {
            Connection connection = DriverManager.getConnection(connectionString);
            System.out.println("Connection to SQLite has been established.");
            conn = connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    // Schema function to reset the database if needed - do not change
    public static void resetDatabase() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
        connect();
        applySchema();
        seedDatabase();
    }

    // Schema function to reset the database if needed - do not change
    private static void applySchema() {
        String itemsSql = "CREATE TABLE IF NOT EXISTS items (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String inventorySql = "CREATE TABLE IF NOT EXISTS inventory (\n"
                + "id integer PRIMARY KEY,\n"
                + "item integer NOT NULL UNIQUE references items(id) ON DELETE CASCADE,\n"
                + "stock integer NOT NULL,\n"
                + "capacity integer NOT NULL\n"
                + ");";
        String distributorSql = "CREATE TABLE IF NOT EXISTS distributors (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String distributorPricesSql = "CREATE TABLE IF NOT EXISTS distributor_prices (\n"
                + "id integer PRIMARY KEY,\n"
                + "distributor integer NOT NULL references distributors(id) ON DELETE CASCADE,\n"
                + "item integer NOT NULL references items(id) ON DELETE CASCADE,\n"
                + "cost float NOT NULL\n" +
                ");";

        try {
            System.out.println("Applying schema");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Schema applied");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Schema function to reset the database if needed - do not change
    private static void seedDatabase() {
        String itemsSql = "INSERT INTO items (id, name) VALUES (1, 'Licorice'), (2, 'Good & Plenty'),\n"
            + "(3, 'Smarties'), (4, 'Tootsie Rolls'), (5, 'Necco Wafers'), (6, 'Wax Cola Bottles'), (7, 'Circus Peanuts'), (8, 'Candy Corn'),\n"
            + "(9, 'Twix'), (10, 'Snickers'), (11, 'M&Ms'), (12, 'Skittles'), (13, 'Starburst'), (14, 'Butterfinger'), (15, 'Peach Rings'), (16, 'Gummy Bears'), (17, 'Sour Patch Kids')";
        String inventorySql = "INSERT INTO inventory (item, stock, capacity) VALUES\n"
                + "(1, 22, 25), (2, 4, 20), (3, 15, 25), (4, 0, 50), (5, 14, 15), (6, 8, 10), (7, 10, 10), (8, 30, 40), (9, 77, 70), (10, 36, 100),\n" +
                "(11, 32, 55), (12, 25, 45), (13, 0, 45), (14, 99, 60), (15, 34, 100), (16, 0, 35), (17, 35, 100)";
        String distributorSql = "INSERT INTO distributors (id, name) VALUES (1, 'Candy Corp'), (2, 'The Sweet Suite'), (3, 'Dentists Hate Us')";
        String distributorPricesSql = "INSERT INTO distributor_prices (distributor, item, cost) VALUES \n" +
                "(1, 1, 0.81), (1, 2, 0.46), (1, 3, 0.89), (1, 4, 0.45), (2, 2, 0.18), (2, 3, 0.54), (2, 4, 0.67), (2, 5, 0.25), (2, 6, 0.35), (2, 7, 0.23), (2, 8, 0.41), (2, 9, 0.54),\n" +
                "(2, 10, 0.25), (2, 11, 0.52), (2, 12, 0.07), (2, 13, 0.77), (2, 14, 0.93), (2, 15, 0.11), (2, 16, 0.42), (3, 10, 0.47), (3, 11, 0.84), (3, 12, 0.15), (3, 13, 0.07), (3, 14, 0.97),\n" +
                "(3, 15, 0.39), (3, 16, 0.91), (3, 17, 0.85)";

        try {
            System.out.println("Seeding database");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Database seeded");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Helper methods to convert ResultSet to JSON - change if desired, but should not be required
    private static JSONArray convertResultSetToJson(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<String> colNames = IntStream.range(0, columns)
                .mapToObj(i -> {
                    try {
                        return md.getColumnName(i + 1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());

        JSONArray jsonArray = new JSONArray();
        while (rs.next()) {
            jsonArray.add(convertRowToJson(rs, colNames));
        }
        return jsonArray;
    }

    private static JSONObject convertRowToJson(ResultSet rs, List<String> colNames) throws SQLException {
        JSONObject obj = new JSONObject();
        for (String colName : colNames) {
            obj.put(colName, rs.getObject(colName));
        }
        return obj;
    }

    // Helper function to execute a query given a SQL command as the parameter
    public static JSONArray executeQuery(String sql) {
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Helper function that take in a string and return true if it is an integer, and false otherwise
    public static boolean isInt(String num){
        try {
            Integer.parseInt(num);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Helper function that take in a string and return true if it is an float, and false otherwise
    public static boolean isFloat(String num){
        try {
            Float.parseFloat(num);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Controller functions - add your routes here. getItems is provided as an example
    // Funciton connected to route /items. Returns all items in the items table
    public static JSONArray getItems() {
        String sql = "SELECT items.name, items.id, inventory.stock, inventory.capacity FROM inventory JOIN items ON inventory.item = items.id";
        return executeQuery(sql);
    }

    // Funciton connected to route /items/outofstock. Returns all items that have 0 stock
    public static JSONArray getOutOfStock() {
        String sql = "SELECT items.name, items.id, inventory.stock, inventory.capacity FROM inventory JOIN items ON inventory.item = items.id WHERE inventory.stock = 0";
        return executeQuery(sql);
    }

    // Funciton connected to route /items/overstock. Returns all items that have more stock than capacity
    public static JSONArray getOverstock() {
        String sql = "SELECT items.name, items.id, inventory.stock, inventory.capacity FROM inventory JOIN items ON inventory.item = items.id WHERE inventory.stock > inventory.capacity";
        return executeQuery(sql);
    }

    // Funciton connected to route /items/lowstock. Returns all items that have less than 35% stock
    public static JSONArray getLowStock() {
        String sql = "SELECT items.name, items.id, inventory.stock, inventory.capacity FROM inventory JOIN items ON inventory.item = items.id WHERE 1.0 * inventory.stock / inventory.capacity < 0.35";
        return executeQuery(sql);
    }

    // Funciton connected to route /items/:id. Returns an item given an id from the user
    public static JSONArray getItemById(String id) {
        String sql = "SELECT items.name, items.id, inventory.stock, inventory.capacity FROM inventory JOIN items ON inventory.item = items.id WHERE items.id = " + id;
        return executeQuery(sql);
    }

    // Funciton connected to route /distributors. Returns all distributors in the distributors table
    public static JSONArray getDistributors() {
        String sql = "SELECT * FROM distributors";
        return executeQuery(sql);
    }

    // Funciton connected to route /distributors/:id. Returns a distributor given an id from the user
    public static JSONArray getItemsByDistributorId(String id) {
        String sql = "SELECT items.name, items.id, distributor_prices.cost FROM distributor_prices JOIN distributors ON distributor_prices.distributor = distributors.id JOIN items ON distributor_prices.item = items.id WHERE distributors.id = " + id;
        return executeQuery(sql);
    }

    // Funciton connected to route /distributors/item/:id. Returns all costs and distributor information of an item given an item id from the user
    public static JSONArray getDistributorsByItemId(String id) {
        String sql = "SELECT distributors.name, distributors.id, distributor_prices.cost from distributors JOIN distributor_prices ON distributors.id  = distributor_prices.distributor JOIN items ON distributor_prices.item = items.id WHERE items.id = " + id;
        return executeQuery(sql);
    }

    // Funciton connected to route /items/cheapest/:id. Returns the cheapest cost of each distributor to restock an item to full capacity given an item id from the user
    public static JSONArray getCheapestPrice(String id) {
        String sql = "SELECT distributors.name, (MIN(distributor_prices.cost) * (inventory.capacity - inventory.stock)) AS price FROM distributor_prices JOIN distributors ON distributor_prices.distributor = distributors.id JOIN items ON distributor_prices.item = items.id JOIN inventory ON items.id = inventory.item WHERE items.id = " + id + " GROUP BY distributors.id";
        return executeQuery(sql);
    }
    
    // Function connected to route /items/add. Add item to items table given name from user
    public static JSONArray postItem(String name) {
        JSONArray arr = new JSONArray();
        if(name.isBlank()) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Item couldn't be added, item name can't be blank");
            return arr;
        }
        String query = "SELECT * FROM items WHERE name = '" + name + "'";
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Item couldn't be added, name already exists");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: Item couldn't be added, an unexpected error occured");
            return arr;
        }
        String sql = "INSERT INTO items (name) VALUES ('" + name + "')";
        try {
            conn.createStatement().execute(sql);
            arr.add("Status Code: 200");
            arr.add("Item successfully added");
            return arr;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: Item couldn't be added, an unexpected error occured");
            return arr;
        }
    }


    // Function connected to route /items/addtoinventory. Add item to inventory table given itemId, stock, and capacity from user
    public static JSONArray postItemToInventory(String itemId, String stock, String capacity) {
        JSONArray arr = new JSONArray();
        if(!isInt(itemId)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Item couldn't be added, item id is not of type Integer");
            return arr;
        }
        if(!isInt(stock)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Item couldn't be added, stock is not of type Integer");
            return arr;
        }   
        if(!isInt(capacity)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Item couldn't be added, capacity is not of type Integer");
            return arr;
        }
        String query = "SELECT * FROM items WHERE id = " + itemId;
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(!set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Item couldn't be added, item id does not match any records");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }

        String sql = "INSERT INTO inventory (item, stock, capacity) VALUES (" + itemId + ", " + stock + ", " + capacity + ")";
        try {
            conn.createStatement().execute(sql);
            arr.add("Status Code: 200");
            arr.add("Item successfully added");
            return arr;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: Item couldn't be added, an unexpected error occured");
            return arr;
        }
    }

    // Function connected to route /items/updated/:id. Update stock or capacity of item in inventory given id, stock, and capacity from user
    public static JSONArray updateItemInInventory(String id, String stock, String capacity) {
        JSONArray arr = new JSONArray();
        if(!isInt(stock)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Item couldn't be updated, stock is not of type Integer");
            return arr;
        }
        if(!isInt(capacity)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Item couldn't be updated, capacity is not of type Integer");
            return arr;
        }
        String query = "SELECT * FROM inventory WHERE item = " + id;
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(!set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Item couldn't be updated, id doesn't match any records");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
        String sql = "UPDATE inventory SET stock = " + stock + ", capacity = " + capacity + " WHERE item = " + id;
        try {
            conn.createStatement().execute(sql);
            arr.add("Status Code: 200");
            arr.add("Item updated successfully");
            return arr;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
    }

    // Function connected to route /distributors/add. Add distributor to distributors table given id and name from user
    public static JSONArray postDistributor(String name) {
        JSONArray arr = new JSONArray();
        if(name.isBlank()) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Distributor couldn't be added, distributor name can't be blank");
            return arr;
        }
        String query = "SELECT * FROM distributors WHERE name = '" + name + "'";
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Distributor couldn't be added, name already exists");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
        String sql = "INSERT INTO distributors (name) VALUES ('" + name + "')";
        try {
            conn.createStatement().execute(sql);
            arr.add("Status Code: 200");
            arr.add("Distributor successfully added");
            return arr;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
    }

    // Function connected to route /distributors/prices/add. Add distributor price to distributor_prices table given distributor, item, and cost from user
    public static JSONArray postDistributorPrice(String distributor, String item, String cost) {
        JSONArray arr = new JSONArray();
        if(!isInt(distributor)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Distributor Price couldn't be added, distributor is not of type Integer");
            return arr;
        }
        if(!isInt(item)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Distributor Price couldn't be added, item is not of type Integer");
            return arr;
        }
        if(!isFloat(cost)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Distributor Price couldn't be added, cost is not of type Float");
            return arr;
        }
        String query = "SELECT * FROM distributors WHERE id = " + distributor;
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(!set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Distributor Prices couldn't be added, distributor doesn't exist");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }

        query = "SELECT * FROM items WHERE id = " + item;
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(!set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Distributor Prices couldn't be added, item doesn't exist");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
        String sql = "INSERT INTO distributor_prices (distributor, item, cost) VALUES (" + distributor + ", " + item + ", " + cost +")";
        try {
            conn.createStatement().execute(sql);
            arr.add("Status Code: 200");
            arr.add("Distributor Prices successfully added");
            return arr;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
    }

    // Function connceted to route /distributors/price/updated/:id. Update cost of item in distributor_prices table given id, item, and cost from user
    public static JSONArray updateDistributorPrice(String id, String item, String cost) {
        JSONArray arr = new JSONArray();
        if(!isInt(id)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Distributor Price couldn't be updated, id is not of type Integer");
            return arr;
        }
        if(!isInt(item)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Distributor Price couldn't be updated, item is not of type Integer");
            return arr;
        }
        if(!isFloat(cost)) {
            arr.add("Status Code: 400");
            arr.add("Error Message: Distributor Price couldn't be updated, cost is not of type Float");
            return arr;
        }
        String query = "SELECT * FROM distributor_prices WHERE distributor = " + id + " AND item = " + item;
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(!set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Distributor Price couldn't be updated, id doesn't match any records");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
        String sql = "UPDATE distributor_prices SET cost = " + cost + " WHERE distributor = " + id + " AND item = " + item;
        try {
            conn.createStatement().execute(sql);
            arr.add("Status Code: 200");
            arr.add("Distributor Price successfully updated");
            return arr;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
    }

    // Function connected to route /items/delete/:id. Delete item from items table given id from user
    public static JSONArray deleteItemFromInventory(String id) {
        JSONArray arr = new JSONArray();
        String query = "SELECT * FROM items WHERE id = " + id;
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(!set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Item couldn't be deleted, id does not match any records");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }

        String sql = "DELETE FROM items WHERE id = " + id;
        try {
            conn.createStatement().execute(sql);
            arr.add("Status Code: 200");
            arr.add("Item successfully deleted");
            return arr;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
    }

    // Function connected to route /distributors/delete/:id. Delete distributor from distributors table given id from user
    public static JSONArray deleteDistributor(String id) {
        JSONArray arr = new JSONArray();
        String query = "SELECT * FROM distributors WHERE id = " + id;
        try {
            ResultSet set = conn.createStatement().executeQuery(query);
            if(!set.isBeforeFirst()) {
                arr.add("Status Code: 400");
                arr.add("Error Message: Distributor couldn't be deleted, id does not match any records");
                return arr;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }

        String sql = "DELETE FROM distributors WHERE id = " + id;
        try {
            conn.createStatement().execute(sql);
            arr.add("Status Code: 200");
            arr.add("Distributor successfully deleted");
            return arr;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            arr.add("Status Code: 500");
            arr.add("Error Message: an unexpected error occured");
            return arr;
        }
    }

}