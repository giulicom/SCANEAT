package com.app.project.scaneatapp.db_products_in_list;

public class ProductInList {
    private int id_prod;
    private int id_list;
    private long barcode;
    private String name;
    private String brand;
    private int quantity;
    private int noGluten;
    private int noLactose;
    private int vegan;
    private int isFavourite;


    public ProductInList(int id_prod, int id_list, long barcode, String name, String brand, int quantity, int noGluten, int noLactose, int vegan, int favourite) {
        this.id_prod = id_prod;
        this.id_list = id_list;
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.noGluten = noGluten;
        this.noLactose = noLactose;
        this.vegan = vegan;
        this.isFavourite = favourite;
    }

    public ProductInList(int id_list, long barcode, String name, String brand, int quantity, int noGluten, int noLactose, int vegan, int favourite) {
        this.id_list = id_list;
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.noGluten = noGluten;
        this.noLactose = noLactose;
        this.vegan = vegan;
        this.isFavourite = favourite;
    }


    public ProductInList(int id_list, String name, String brand, int quantity, int noGluten, int noLactose, int vegan) {
        this.id_prod = -1;
        this.id_list = id_list;
        this.barcode = -1;
        this.name = name;
        this.brand = brand;
        this.quantity=quantity;
        this.noGluten = noGluten;
        this.noLactose = noLactose;
        this.vegan = vegan;
        this.isFavourite = 0;
    }

    public ProductInList(int id_prod, int id_list, String name, String brand, int quantity, int noGluten, int noLactose, int vegan) {
        this.id_prod = id_prod;
        this.id_list = id_list;
        this.barcode = -1;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.noGluten = noGluten;
        this.noLactose = noLactose;
        this.vegan = vegan;
        this.isFavourite = 0;
    }

    public ProductInList(int id_list, long barcode, String name, String brand, int quantity, int noGluten, int noLactose, int vegan) {
        this.id_prod = -1;
        this.id_list = id_list;
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.quantity=quantity;
        this.noGluten = noGluten;
        this.noLactose = noLactose;
        this.vegan = vegan;
        this.isFavourite = 0;
    }


    public int getIdProd() {return this.id_prod;}
    public void setIdProd(int id_prod) {this.id_prod = id_prod;}
    public int getIdList(){return this.id_list;}
    public void setIdList(int id_list) {this.id_list = id_list;}
    public long getBarcode(){return this.barcode;}
    public void setBarcode(long barcode){this.barcode=barcode;}
    public String getName() {return this.name;}
    public void setName(String name) {this.name = name;}
    public String getBrand(){return this.brand;}
    public void setBrand(String brand){this.brand = brand;}
    public int getQuantity(){return this.quantity;}
    public void setQuantity(int quantity){this.quantity=quantity;}
    public int getNoGluten(){return this.noGluten;}
    public void setNoGluten(int noGluten){this.noGluten = noGluten;}
    public int getNoLactose(){return this.noLactose;}
    public void setNoLactose(int noLactose){this.noLactose = noLactose;}
    public int getVegan(){return this.vegan;}
    public void setVegan(int vegan){this.vegan = vegan;}
    public int getIsFavourite(){return this.isFavourite;}
    public void setIsFavourite(int isFavourite){this.isFavourite = isFavourite;}

    @Override
    public String toString() {return this.id_list + " - " + this.name + " - "  + this.isFavourite;}
}
