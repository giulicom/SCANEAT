package com.app.project.scaneatapp.db_products_in_list;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.app.project.scaneatapp.MyApplication;
import com.app.project.scaneatapp.db_products.Product;

import java.util.ArrayList;
import java.util.List;


public class MyProductInList_impl implements MyProductInList_DAO {
    private SQLiteDatabase database_product;
    private MyProductInList_SQLiteHelper dbHelper;
    private String[] allColumns = {MyProductInList_SQLiteHelper.COLUMN_ID,MyProductInList_SQLiteHelper.COLUMN_ID_LIST,
            MyProductInList_SQLiteHelper.COLUMN_BARCODE, MyProductInList_SQLiteHelper.COLUMN_NAME,
            MyProductInList_SQLiteHelper.COLUMN_BRAND, MyProductInList_SQLiteHelper.COLUMN_QUANTITY,
            MyProductInList_SQLiteHelper.COLUMN_NO_GLUTEN, MyProductInList_SQLiteHelper.COLUMN_NO_LACTOSE,
            MyProductInList_SQLiteHelper.COLUMN_VEGAN,MyProductInList_SQLiteHelper.COLUMN_FAVOURITE};


    @Override
    public void open() {
        if(dbHelper == null) dbHelper =
                new MyProductInList_SQLiteHelper(MyApplication.getAppContext());
        database_product = dbHelper.getWritableDatabase();
    }

    @Override
    public void close() {dbHelper.close();}

    // from Object to database
    private ContentValues myProductInListToValues(ProductInList prod) {
        ContentValues values = new ContentValues();
        values.put(MyProductInList_SQLiteHelper.COLUMN_ID_LIST, prod.getIdList());
        values.put(MyProductInList_SQLiteHelper.COLUMN_BARCODE, prod.getBarcode());
        values.put(MyProductInList_SQLiteHelper.COLUMN_NAME, prod.getName());
        values.put(MyProductInList_SQLiteHelper.COLUMN_BRAND, prod.getBrand());
        values.put(MyProductInList_SQLiteHelper.COLUMN_QUANTITY, prod.getQuantity());
        values.put(MyProductInList_SQLiteHelper.COLUMN_NO_GLUTEN, prod.getNoGluten());
        values.put(MyProductInList_SQLiteHelper.COLUMN_NO_LACTOSE, prod.getNoLactose());
        values.put(MyProductInList_SQLiteHelper.COLUMN_VEGAN, prod.getVegan());
        values.put(MyProductInList_SQLiteHelper.COLUMN_FAVOURITE, prod.getIsFavourite());

        return values;
    }

    // from database to Object
    private ProductInList cursorToProductInList(Cursor cursor) {
        int id = cursor.getInt(0);
        int id_list = cursor.getInt(1);
        long barcode = cursor.getLong(2);
        String name = cursor.getString(3);
        String brand = cursor.getString(4);
        int quantity = cursor.getInt(5);
        int no_gluten = cursor.getInt(6);
        int no_lactose = cursor.getInt(7);
        int vegan = cursor.getInt(8);
        int favourite = cursor.getInt(9);

        return new ProductInList(id,id_list,barcode,name,brand,quantity, no_gluten, no_lactose, vegan, favourite);
    }

    @Override
    public ProductInList insertProduct(ProductInList prod) {
        long insertId = database_product.insert(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, null, myProductInListToValues(prod));
        // now read from DB the inserted person and return it
        Cursor cursor = database_product.query(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, allColumns,
                MyProductInList_SQLiteHelper.COLUMN_ID + " = ?",
                new String[]{"" + insertId}, null, null, null);
        cursor.moveToFirst();
        ProductInList p = cursorToProductInList(cursor);
        cursor.close();
        return p;
    }

    @Override
    public void deleteProduct(ProductInList prod) {
        int id = prod.getIdProd();
        int isFavourite = prod.getIsFavourite();

        // IF IS NOT FAVOURITE, DELETE IT
        if(isFavourite == 0) {
            String whereClause = MyProductInList_SQLiteHelper.COLUMN_ID + " = ? ";
            String[] whereArgs = new String[] {""+id};
            database_product.delete(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST,
                    whereClause, whereArgs);
        }
        // ELSE CHANGE IDLIST TO -1 => UPDATE
        else {
            ContentValues values = new ContentValues();
            values.put(MyProductInList_SQLiteHelper.COLUMN_ID_LIST, -1);

            int idList = prod.getIdList();
            String whereClause = MyProductInList_SQLiteHelper.COLUMN_ID_LIST + " = ? AND " +
                    MyProductInList_SQLiteHelper.COLUMN_ID + " = ? ";
            String[] whereArgs = new String[] {""+idList,""+id};

            database_product.update(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST,values,whereClause,whereArgs);
        }

    }

    @Override
    public int countElementsOfAList(int idList) {
        List<ProductInList> lists = getAllProductsInList(idList);
        return lists.size();
    }

    // EDIT AN EXISTING PRODUCT
    @Override
    public void editProduct(ProductInList prod) {
        int id_prod = prod.getIdProd();

        String whereClause = MyProductInList_SQLiteHelper.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{ ""+id_prod};
        // query
        database_product.update(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, myProductInListToValues(prod), whereClause, whereArgs);
        Log.d("editProduct", prod.getName() + " " + prod.getBrand());
    }

    // EDIT PRODUCTS WHEN UPDATE THE EXTERNAL PRODUCTS DB
    @Override
    public void editProductByBarcode(Product prod){
        ContentValues values = new ContentValues();
        values.put(MyProductInList_SQLiteHelper.COLUMN_NAME, prod.getName().toString());
        values.put(MyProductInList_SQLiteHelper.COLUMN_BRAND, prod.getBrand().toString());
        values.put(MyProductInList_SQLiteHelper.COLUMN_NO_GLUTEN, prod.getNoGluten());
        values.put(MyProductInList_SQLiteHelper.COLUMN_NO_LACTOSE, prod.getNoLactose());
        values.put(MyProductInList_SQLiteHelper.COLUMN_VEGAN, prod.getVegan());

        long barcode = prod.getBarcode();
        String whereClause = MyProductInList_SQLiteHelper.COLUMN_BARCODE + " = ? ";
        String[] whereArgs = new String[] {""+barcode};

        database_product.update(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST,values,whereClause,whereArgs);
    }

    // SET FAVOURITE
    @Override
    public void setFavourite(ProductInList prod) {
        long barcode = prod.getBarcode();
        // se il prodotto viene dal db "esterno" setto "favourite" di tutti i prodotti con lo stesso barcode
        if(barcode!=-1) {
            String whereClause = MyProductInList_SQLiteHelper.COLUMN_BARCODE + " = ? ";
            String[] whereArgs = new String[]{ ""+barcode};
            // campi che voglio modificare
            ContentValues values = new ContentValues();
            values.put(MyProductInList_SQLiteHelper.COLUMN_FAVOURITE, prod.getIsFavourite());
            Log.d("setFavourite",prod.getName()+" - "+prod.getIsFavourite());
            // query
            database_product.update(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, values, whereClause, whereArgs);
        }
        else { // se il prodotto è stato inserito manualmente
            String name = prod.getName();
            String brand = prod.getBrand();
            int noGluten = prod.getNoGluten();
            int noLactose = prod.getNoLactose();
            int vegan = prod.getVegan();

            String whereClause = MyProductInList_SQLiteHelper.COLUMN_NAME + " = ? AND " +
                    MyProductInList_SQLiteHelper.COLUMN_BRAND + " = ? AND " +
                    MyProductInList_SQLiteHelper.COLUMN_NO_GLUTEN + " = ? AND " +
                    MyProductInList_SQLiteHelper.COLUMN_NO_LACTOSE + " = ? AND " +
                    MyProductInList_SQLiteHelper.COLUMN_VEGAN + " = ? ";

            String[] whereArgs = new String[]{name,brand,""+noGluten,""+noLactose,""+vegan};

            // campi che voglio modificare
            ContentValues values = new ContentValues();
            values.put(MyProductInList_SQLiteHelper.COLUMN_FAVOURITE, prod.getIsFavourite());
            Log.d("setFavourite", prod.getName() + " - " + prod.getIsFavourite());
            // query
            database_product.update(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, values, whereClause, whereArgs);
        }
    }



    @Override
    public int countFavourites() {
        List<ProductInList> fav = getFavourites();
        return fav.size();
    }

    //GET ALL FAVOURITES
    @Override
    public List<ProductInList> getFavourites() {
        List<ProductInList> lists = new ArrayList<ProductInList>();

        String selectionClause = MyProductInList_SQLiteHelper.COLUMN_FAVOURITE + " = ? AND " +
                MyProductInList_SQLiteHelper.COLUMN_BARCODE + " != ?";
        String[] selectionArgs = new String[]{"1", "-1"};
        // SELECT DISTINCT QUERY (show only product with distinct barcodes)
        Cursor cursor = database_product.query(true, MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST,
                allColumns,selectionClause,selectionArgs,MyProductInList_SQLiteHelper.COLUMN_BARCODE,null,null,null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            ProductInList prod = cursorToProductInList(cursor);
            lists.add(prod);
            cursor.moveToNext();
        }
        cursor.close();

        String selectionClause2 = MyProductInList_SQLiteHelper.COLUMN_FAVOURITE + " = ? AND " +
                MyProductInList_SQLiteHelper.COLUMN_BARCODE + " = ? ";
        String[] selectionArgs2 = new String[]{"1","-1"};
        // SELECT DISTINCT QUERY (show only product with distinct names)
        Cursor cursor2 = database_product.query(true, MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST,
                allColumns, selectionClause2, selectionArgs2, MyProductInList_SQLiteHelper.COLUMN_NAME, null, null, null);

        cursor2.moveToFirst();
        while(!cursor2.isAfterLast()) {
            ProductInList prod = cursorToProductInList(cursor2);
            lists.add(prod);
            cursor2.moveToNext();
        }
        cursor2.close();

        return lists;

    }

    @Override
    public void deleteProductsOfAListNotFavourites(int idList) {
        String whereClause = MyProductInList_SQLiteHelper.COLUMN_ID_LIST + " = ? AND " + MyProductInList_SQLiteHelper.COLUMN_FAVOURITE + " = ?";
        String[] whereArgs = new String[]{ ""+idList,"0"};
        database_product.delete(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, whereClause, whereArgs);
    }

    // GET ALL PRODUCTS IN A LIST
    @Override
    public List<ProductInList> getAllProductsInList(int id_list) {
        List<ProductInList> lists = new ArrayList<ProductInList>();

        String whereClause = MyProductInList_SQLiteHelper.COLUMN_ID_LIST + " = ? ";
        String[] whereArgs = new String[]{ ""+id_list};

        Cursor cursor = database_product.query(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST,
                allColumns, whereClause, whereArgs, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            ProductInList prod = cursorToProductInList(cursor);
            lists.add(prod);
            cursor.moveToNext();
        }
        cursor.close();
        return lists;
    }

    // get ALL products (serve per verificare tutti i prodotti presenti)
    @Override
    public List<ProductInList> getAllProducts() {
        List<ProductInList> lists = new ArrayList<ProductInList>();
        Cursor cursor = database_product.query(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            ProductInList prod = cursorToProductInList(cursor);
            Log.d("whileee", prod.toString());
            lists.add(prod);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return lists;
    }

    @Override
    public String insertExtProductFromInfoDialog(ProductInList prod) {
        checkExistence(prod);
        long insertId = database_product.insert(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, null, myProductInListToValues(prod));
        // now read from DB the inserted person and return it
        Cursor cursor = database_product.query(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, allColumns,
                MyProductInList_SQLiteHelper.COLUMN_ID + " = ?",
                new String[]{"" + insertId}, null, null, null);
        cursor.moveToFirst();
        ProductInList p = cursorToProductInList(cursor);
        cursor.close();
        return p.getName();

    }

    @Override
    public String insertListProductFromInfoDialog(int idProd, int idList, int quantity) {
        //checkExistence();
        String whereClause = MyProductInList_SQLiteHelper.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(idProd)};
        Cursor cursor = database_product.query(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, allColumns,
                whereClause, whereArgs, null, null, null);
        cursor.moveToFirst();
        ProductInList p = cursorToProductInList(cursor);
        cursor.close();

        p.setIdList(idList);
        p.setQuantity(quantity);
        database_product.insert(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, null, myProductInListToValues(p));
        return p.getName();
    }

    @Override
    public boolean checkExistence(ProductInList prod) {
        long barcode = prod.getBarcode();
        if(barcode!=-1) {
            String whereClause = MyProductInList_SQLiteHelper.COLUMN_BARCODE + " = ?";
            String[] whereArgs = new String[]{String.valueOf(barcode)};
            Cursor cursor = database_product.query(MyProductInList_SQLiteHelper.TABLE_PRODUCTS_IN_LIST, allColumns,
                    whereClause, whereArgs, null, null, null);
            try {
                cursor.moveToFirst();
                // take the first
                ProductInList p = cursorToProductInList(cursor);
                cursor.close();
                // check if there is another product with the same barcode that is within "favourites"
                // I need only the first because if one was marked as "favourite" also the other are between "favourites"
                if(p.getIsFavourite()==1){
                    prod.setIsFavourite(1);
                    return true;
                }
                else return false;

            }
            catch(Exception e)
            {
                Log.d("checkExistence","Questo prodotto è nuovo!");
            }
        }
        return false;
    }
}

