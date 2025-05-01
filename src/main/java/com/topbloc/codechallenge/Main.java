package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;
import static spark.Spark.put;

public class Main {
    public static void main(String[] args) {
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
}