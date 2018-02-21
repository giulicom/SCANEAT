package com.app.project.scaneatapp.db_products;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.project.scaneatapp.MyApplication;

import java.util.ArrayList;
import java.util.List;


public class MyProduct_impl implements MyProduct_DAO {
    private SQLiteDatabase database_product;
    private MyProduct_SQLiteHelper dbHelper;
    private String[] allColumns = {MyProduct_SQLiteHelper.COLUMN_BARCODE, MyProduct_SQLiteHelper.COLUMN_NAME,
            MyProduct_SQLiteHelper.COLUMN_BRAND, MyProduct_SQLiteHelper.COLUMN_INGREDIENTS,
            MyProduct_SQLiteHelper.COLUMN_NO_GLUTEN, MyProduct_SQLiteHelper.COLUMN_NO_LACTOSE, MyProduct_SQLiteHelper.COLUMN_VEGAN,
            MyProduct_SQLiteHelper.COLUMN_USER};


    @Override
    public void open() {
        if(dbHelper == null) dbHelper =
                new MyProduct_SQLiteHelper(MyApplication.getAppContext());
        database_product = dbHelper.getWritableDatabase();
    }

    @Override
    public void close() {dbHelper.close();}

    // GET ALL PRODUCTS IN A LIST
    @Override
    public List<Product> getFilteredProducts(String search) {
        List<Product> list = new ArrayList<Product>();

        String whereClauseName = MyProduct_SQLiteHelper.COLUMN_NAME + " LIKE ?";
        String whereClauseBrand = MyProduct_SQLiteHelper.COLUMN_BRAND + " LIKE ?";
        String[] whereArgs = new String[]{ ""+search};

        Cursor cursorName = database_product.query(true, MyProduct_SQLiteHelper.TABLE_PRODUCTS,
                allColumns, whereClauseName, new String[] {"%" + search + "%"}, null, null, null, null);
        cursorName.moveToFirst();
        while(!cursorName.isAfterLast()) {
            Product prod = cursorToProduct(cursorName);
            list.add(prod);
            cursorName.moveToNext();
        }
        cursorName.close(); // remember to always close the cursor!
        Cursor cursorBrand = database_product.query(true, MyProduct_SQLiteHelper.TABLE_PRODUCTS,
                allColumns, whereClauseBrand, new String[] {"%" + search + "%"}, null, null, null, null);
        cursorBrand.moveToFirst();
        while(!cursorBrand.isAfterLast()) {
            Product prod = cursorToProduct(cursorBrand);
            list.add(prod);
            cursorBrand.moveToNext();
        }
        cursorBrand.close();

        list = removeDoublesAndOrder(list);

        return list;
    }

    @Override
    public Product getProduct(long barcode) {
        Cursor cursor = database_product.query(MyProduct_SQLiteHelper.TABLE_PRODUCTS, allColumns,
                MyProduct_SQLiteHelper.COLUMN_BARCODE + " = ?",
                new String[]{"" + barcode}, null, null, null);
        cursor.moveToFirst();
        Product p = cursorToProduct(cursor);
        cursor.close();
        return p;
    }


    @Override
    public void deleteAllProducts() {
        database_product.delete(MyProduct_SQLiteHelper.TABLE_PRODUCTS, "1", null);
    }


    // from Object to database
    private ContentValues myProductToValues(Product prod) {
        ContentValues values = new ContentValues();
        values.put(MyProduct_SQLiteHelper.COLUMN_BARCODE, prod.getBarcode());
        values.put(MyProduct_SQLiteHelper.COLUMN_NAME, prod.getName());
        values.put(MyProduct_SQLiteHelper.COLUMN_BRAND, prod.getBrand());
        values.put(MyProduct_SQLiteHelper.COLUMN_INGREDIENTS, prod.getIngredients());
        values.put(MyProduct_SQLiteHelper.COLUMN_NO_GLUTEN, prod.getNoGluten());
        values.put(MyProduct_SQLiteHelper.COLUMN_NO_LACTOSE, prod.getNoLactose());
        values.put(MyProduct_SQLiteHelper.COLUMN_VEGAN, prod.getVegan());
        values.put(MyProduct_SQLiteHelper.COLUMN_USER, prod.getUser());

        return values;
    }

    // from database to Object
    private Product cursorToProduct(Cursor cursor) {
        if(cursor.getCount() > 0) {
            long barcode = cursor.getLong(0);
            String name = cursor.getString(1);
            String brand = cursor.getString(2);
            String ingredients = cursor.getString(3);
            int no_gluten = cursor.getInt(4);
            int no_lactose = cursor.getInt(5);
            int vegan = cursor.getInt(6);
            String user = cursor.getString(7);
            return new Product(barcode,name,brand, ingredients, no_gluten, no_lactose, vegan, user);
        }

        return null;

    }


    @Override
    public void insertProduct(Product prod) {
        long barcode = database_product.insert(MyProduct_SQLiteHelper.TABLE_PRODUCTS, null, myProductToValues(prod));
        // now read from DB the inserted person and return it

        Cursor cursor = database_product.query(MyProduct_SQLiteHelper.TABLE_PRODUCTS, allColumns,
                MyProduct_SQLiteHelper.COLUMN_BARCODE + " = ?",
                new String[]{"" + barcode}, null, null, null);
        cursor.moveToFirst();
        Product p = cursorToProduct(cursor);

        cursor.close();
    }

    // GET ALL PRODUCTS
    @Override
    public List<Product> getAllProducts() {
        List<Product> lists = new ArrayList<Product>();

        Cursor cursor = database_product.query(MyProduct_SQLiteHelper.TABLE_PRODUCTS,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Product prod = cursorToProduct(cursor);
            lists.add(prod);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return lists;
    }

    @Override
    public int countProducts() {
        List<Product> products = getAllProducts();
        return products.size();
    }

    private List<Product>  removeDoublesAndOrder(List<Product> prod) {
        Product temp;
        for(int i = 0; i < prod.size(); i++)
        {
            temp = prod.get(i);
            for(int j = i+1; j < prod.size(); j++) {
                if(temp.getBarcode() == prod.get(j).getBarcode()) {
                    prod.remove(j);
                    j--;
                }

            }
        }
        for(int i = 0; i < prod.size(); i++){
            for(int j = (prod.size()-1); j>i; j--){
                if(prod.get(j).getName().compareToIgnoreCase(prod.get(i).getName()) < 0)
                {
                    temp = prod.get(j);
                    prod.remove(j);
                    prod.add(j, prod.get(i));
                    prod.remove(i);
                    prod.add(i, temp);
                }
            }

        }
        return prod;
    }



    @Override
    public void updateProduct(Product prod) {
        long id_prod = prod.getBarcode();

        String whereClause = MyProduct_SQLiteHelper.COLUMN_BARCODE + " = ?";
        String[] whereArgs = new String[]{ ""+id_prod};
        // query
        database_product.update(MyProduct_SQLiteHelper.TABLE_PRODUCTS, myProductToValues(prod), whereClause, whereArgs);
        Log.d("editProduct", prod.getName() + " " + prod.getBrand());
    }

    @Override
    public List<Product> getUserProducts(String user) {
        List<Product> list = new ArrayList<Product>();

        String whereClause = MyProduct_SQLiteHelper.COLUMN_USER + " = ? ";
        String[] whereArgs = new String[]{ ""+user};

        Cursor cursor = database_product.query(true, MyProduct_SQLiteHelper.TABLE_PRODUCTS,
                allColumns, whereClause, whereArgs, null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Product prod = cursorToProduct(cursor);
            list.add(prod);
            cursor.moveToNext();
        }
        cursor.close();

        list = removeDoublesAndOrder(list);

        return list;
    }
}
