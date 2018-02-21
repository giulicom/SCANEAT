package com.app.project.scaneatapp.db_products_in_list;

import com.app.project.scaneatapp.db_products.Product;

import java.util.List;


public interface MyProductInList_DAO {
    void open();
    void close();

    ProductInList insertProduct(ProductInList prod);
    void editProduct(ProductInList prod);
    void editProductByBarcode(Product p);
    void deleteProductsOfAListNotFavourites(int idList);
    void deleteProduct(ProductInList prod);
    int countElementsOfAList(int idList);
    void setFavourite(ProductInList prod);
    int countFavourites();
    boolean checkExistence(ProductInList prod);
    List<ProductInList> getFavourites();
    List<ProductInList> getAllProductsInList(int idList);
    List<ProductInList> getAllProducts();
    String insertExtProductFromInfoDialog(ProductInList prod);
    String insertListProductFromInfoDialog(int idProd, int idList, int quantity);
}
