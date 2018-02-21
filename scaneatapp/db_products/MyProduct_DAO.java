package com.app.project.scaneatapp.db_products;

import java.util.List;


public interface MyProduct_DAO {
    public void open();
    public void close();

    public void insertProduct(Product prod);
    public int countProducts();
    public List<Product> getAllProducts();
    public List<Product> getFilteredProducts(String search);
    public Product getProduct(long barcode);
    public void deleteAllProducts();
    public void updateProduct(Product prod);
    public List<Product> getUserProducts(String user);
}
