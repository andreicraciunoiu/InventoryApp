package com.example.android.electronicsinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.electronicsinventory.data.ProductContract.ProductEntry;

class ProductDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "electronics_inventory.db";
    private static final int DATABASE_VERSION = 1;

    ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE + " INTEGER NOT NULL DEFAULT 0, " +
                ProductEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE + " TEXT, " +
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL + " TEXT " +
                ");";
        db.execSQL(CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}