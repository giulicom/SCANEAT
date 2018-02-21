package com.app.project.scaneatapp.db_products;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MyProduct_SQLiteHelper extends SQLiteOpenHelper{
    public static final String TABLE_PRODUCTS = "products_ext";
    public static final String COLUMN_BARCODE="barcode";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BRAND="brand";
    public static final String COLUMN_INGREDIENTS = "ingredients";
    public static final String COLUMN_NO_GLUTEN="no_gluten";
    public static final String COLUMN_NO_LACTOSE="no_lactose";
    public static final String COLUMN_VEGAN="vegan";
    public static final String COLUMN_USER="user";
    public static final String DATABASE_NAME = "products_ext.db";
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_PRODUCT_CREATE = "create table "
            + TABLE_PRODUCTS + "("
            + COLUMN_BARCODE + " real primary key,"
            + COLUMN_NAME + " text not null,"
            + COLUMN_BRAND + " text,"
            + COLUMN_INGREDIENTS + " text,"
            + COLUMN_NO_GLUTEN + " integer not null,"
            + COLUMN_NO_LACTOSE + " integer not null,"
            + COLUMN_VEGAN + " integer not null,"
            + COLUMN_USER + " text);";


    public MyProduct_SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL(DATABASE_PRODUCT_CREATE);}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MyProduct_SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }
}
