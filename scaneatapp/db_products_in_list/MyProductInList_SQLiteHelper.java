package com.app.project.scaneatapp.db_products_in_list;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyProductInList_SQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_PRODUCTS_IN_LIST = "products";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ID_LIST = "id_list";
    public static final String COLUMN_BARCODE="barcode";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BRAND="brand";
    public static final String COLUMN_QUANTITY="quantity";
    public static final String COLUMN_NO_GLUTEN="no_gluten";
    public static final String COLUMN_NO_LACTOSE="no_lactose";
    public static final String COLUMN_VEGAN="vegan";
    public static final String COLUMN_FAVOURITE="favourite";
    public static final String DATABASE_NAME = "products_in_list.db";
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_PRODUCT_CREATE = "create table "
            + TABLE_PRODUCTS_IN_LIST + "("
            + COLUMN_ID + " integer primary key autoincrement,"
            + COLUMN_ID_LIST + " integer not null,"
            + COLUMN_BARCODE + " real,"
            + COLUMN_NAME + " text not null,"
            + COLUMN_BRAND + " text,"
            + COLUMN_QUANTITY + " integer not null,"
            + COLUMN_NO_GLUTEN + " integer not null,"
            + COLUMN_NO_LACTOSE + " integer not null,"
            + COLUMN_VEGAN + " integer not null,"
            + COLUMN_FAVOURITE + " integer not null);";


    public MyProductInList_SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { db.execSQL(DATABASE_PRODUCT_CREATE);}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MyProductInList_SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS_IN_LIST);
        onCreate(db);
    }
}


