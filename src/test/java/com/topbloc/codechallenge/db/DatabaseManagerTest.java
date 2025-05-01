package com.topbloc.codechallenge.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;
import static spark.Spark.put;

public class DatabaseManagerTest {

    public void setupDatabase() {
        DatabaseManager.connect();
        // Don't change this - required for GET and POST requests with the header 'content-type'
        options("/*",
                (req, res) -> {
                    res.header("Access-Control-Allow-Headers", "content-type");
                    res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                    return "OK";
                });

        // Don't change - if required you can reset your database by hitting this endpoint at localhost:4567/reset
        get("/reset", (req, res) -> {   
            DatabaseManager.resetDatabase();
            return "OK";
        });

        //TODO: Add your routes here. a couple of examples are below
        get("/items", (req, res) -> DatabaseManager.getItems());
        get("/items/outofstock", (req, res) -> DatabaseManager.getOutOfStock());
        get("/items/overstock", (req, res) -> DatabaseManager.getOverstock());
        get("/items/lowstock", (req, res) -> DatabaseManager.getLowStock());
        get("/items/:id", (req, res) -> DatabaseManager.getItemById(req.params(":id")));
        get("/distributors", (req, res) -> DatabaseManager.getDistributors());
        get("/distributors/:id", (req, res) -> DatabaseManager.getItemsByDistributorId(req.params(":id")));
        get("/distributors/item/:id", (req, res) -> DatabaseManager.getDistributorsByItemId(req.params(":id")));
        post("/items/add", (req, res) -> DatabaseManager.postItem(req.queryParams("name")));
        post("/items/addtoinventory", (req, res) -> DatabaseManager.postItemToInventory(req.queryParams("itemId"), req.queryParams("stock"), req.queryParams("capacity")));
        put("/items/update/:id", (req, res) -> DatabaseManager.updateItemInInventory(req.params(":id"), req.queryParams("stock"), req.queryParams("capacity")));
        post("/distributors/add", (req, res) -> DatabaseManager.postDistributor(req.queryParams("name")));
        post("/distributors/prices/add", (req, res) -> DatabaseManager.postDistributorPrice(req.queryParams("distributor"), req.queryParams("item"), req.queryParams("cost")));
        put("/distributors/prices/update/:id", (req, res) -> DatabaseManager.updateDistributorPrice(req.params(":id"),  req.queryParams("item"), req.queryParams("cost")));
        get("/items/cheapest/:id", (req, res) -> DatabaseManager.getCheapestPrice(req.params(":id")));
        delete("/items/delete/:id", (req, res) -> DatabaseManager.deleteItemFromInventory(req.params(":id")));
        delete("/distributors/delete/:id", (req, res) -> DatabaseManager.deleteDistributor(req.params(":id")));
        get("/version", (req, res) -> "TopBloc Code Challenge v1.0");
    }


    @Test
    public void testExecuteQueryValidQuery() {
        setupDatabase();    
        String sql = "SELECT * FROM items";
        assertNotNull(DatabaseManager.executeQuery(sql));
    }
    @Test
    public void testExecuteQueryInvalidQuery() {
        setupDatabase();
        String sql = "Select * from item";
        assertNull(DatabaseManager.executeQuery(sql));
    }

    @Test
    public void testGetDistributors() {
        setupDatabase();
        assertNotNull(DatabaseManager.getDistributors());
    }

    @Test
    public void testGetItemByIdValidId() {
        setupDatabase();
        assertNotNull(DatabaseManager.getItemById("1"));
    }

    @Test
    public void testGetItemByIdInvalidId() {
        setupDatabase();
        assertNotNull(DatabaseManager.getItemById("9852352"));
    }

    @Test
    public void testGetItemByIdNonIntegerId() {
        setupDatabase();
        assertNull(DatabaseManager.getItemById("Not a number"));
    }


    @Test
    public void testGetItems() {
        setupDatabase();
        assertNotNull(DatabaseManager.getItems());
    }

    @Test
    public void testGetLowStock() {
        setupDatabase();
        assertNotNull(DatabaseManager.getLowStock());
    }

    @Test
    public void testGetOutOfStock() {
        setupDatabase();
        assertNotNull(DatabaseManager.getOutOfStock());
    }

    @Test
    public void testGetOverstock() {
        setupDatabase();
        assertNotNull(DatabaseManager.getOverstock());
    }

    @Test
    public void testIsFloatWithFloat() {
        setupDatabase();
        assertEquals(true, DatabaseManager.isFloat("3.5"));
    }

    @Test
    public void testIsFloatWithInt() {
        setupDatabase();
        assertEquals(true, DatabaseManager.isInt("3"));
    }

    @Test
    public void testIsFloatWithString() {
        setupDatabase();
        assertEquals(false, DatabaseManager.isInt("3.some decimal"));
    }

    @Test
    public void testIsFloatWithEmpty() {
        setupDatabase();
        assertEquals(false, DatabaseManager.isInt(""));
    }

    @Test
    public void testIsIntWithInt() {
        setupDatabase();
        assertEquals(true, DatabaseManager.isInt("3"));
    }

    @Test
    public void testIsIntWithFloat() {
        setupDatabase();
        assertEquals(false, DatabaseManager.isInt("3.4"));
    }

    @Test
    public void testIsIntWithString() {
        setupDatabase();
        assertEquals(false, DatabaseManager.isInt("dog"));
    }

    @Test
    public void testIsIntWithEmpty() {
        setupDatabase();
        assertEquals(false, DatabaseManager.isInt(""));
    }
}
