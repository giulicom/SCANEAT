package com.app.project.scaneatapp.db_products;


public class Product {
    private long barcode;
    private String name;
    private String brand;
    private String ingredients;
    private int quantity;
    private int noGluten;
    private int noLactose;
    private int vegan;
    private String user;


    public Product(long barcode, String name, String brand, String ingredients, int noGluten, int noLactose, int vegan, String user) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.ingredients = ingredients;
        this.noGluten = noGluten;
        this.noLactose = noLactose;
        this.vegan = vegan;
        this.user = user;
    }

    // costruttore senza user
    public Product(long barcode, String name, String brand, String ingredients, int noGluten, int noLactose, int vegan) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.ingredients = ingredients;
        this.noGluten = noGluten;
        this.noLactose = noLactose;
        this.vegan = vegan;
    }

    // Costruttore senza ingredients
    public Product(long barcode, String name, String brand, int noGluten, int noLactose, int vegan) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.ingredients = "";
        this.noGluten = noGluten;
        this.noLactose = noLactose;
        this.vegan = vegan;
    }


   public long getBarcode(){return this.barcode;}
    public void setBarcode(long barcode){this.barcode=barcode;}
    public String getName() {return this.name;}
    public void setName(String name) {this.name = name;}
    public String getBrand(){return this.brand;}
    public void setBrand(String brand){this.brand = brand;}
    public String getIngredients(){return this.ingredients;}
    public void setIngredients(String ingredients){this.ingredients=ingredients;}
    public int getNoGluten(){return this.noGluten;}
    public void setNoGluten(int noGluten){this.noGluten = noGluten;}
    public int getNoLactose(){return this.noLactose;}
    public void setNoLactose(int noLactose){this.noLactose = noLactose;}
    public int getVegan(){return this.vegan;}
    public void setVegan(int vegan){this.vegan = vegan;}
    public String getUser(){return this.user;}
    public void setUser(String user){this.user = user;}

    @Override
    public String toString() {return this.name + " - " + this.brand + "\nBarcode: " + this.barcode;}
}
