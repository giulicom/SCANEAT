package com.app.project.scaneatapp.db_lists;


public class MyList {
    private int id;
    private String name;


    public MyList(int _id, String _name) {
        this.id = _id;
        this.name = _name;
        //this.products = products;
    }

    public MyList(String _name) {
        this.id = -1;
        this.name = _name;

    }

    public MyList(String _name, String _products) {
        this.name = _name;

        this.id = -1; // means: not in DB
    }

    public int getId() {return this.id;}
    public void setId(int id) {this.id = id;}
    public String getName() {return this.name;}
    public void setName(String name) {this.name = name;}

    @Override
    public String toString() {return this.id + " - " + this.name;}
}
