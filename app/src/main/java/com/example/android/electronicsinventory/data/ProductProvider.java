package com.example.android.electronicsinventory.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.electronicsinventory.R;
import com.example.android.electronicsinventory.data.ProductContract.ProductEntry;

public class ProductProvider extends ContentProvider {

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private ContentResolver contentResolver = null;

    static {
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private ProductDbHelper helper = null;

    @Override
    public boolean onCreate() {
        helper = new ProductDbHelper(getContext());
        Context context = getContext();
        if (context != null) {
            contentResolver = getContext().getContentResolver();
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, null, null, null, null, null, null);
                cursor.setNotificationUri(contentResolver, uri);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(contentResolver, uri);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM;
            default:
                throw new IllegalStateException("getType is not supported for " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                Uri resultedUri = insertProduct(uri, values);
                contentResolver.notifyChange(resultedUri, null);
                return resultedUri;
            default:
                throw new IllegalArgumentException("Cannot insert unknown URI" + uri);
        }
    }

    @Nullable
    private Uri insertProduct(Uri uri, ContentValues contentValues) {
        if (ProductEntry.isAValidProduct(getContext(), contentValues, ProductEntry.PRODUCT_INSERT)) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.product_has_missing_or_invalid_arguments));
        }
        contentValues.remove(ProductEntry.COLUMN_PRODUCT_IMAGE_DUMMY);

        if (contentValues.getAsDouble(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE) == null) {
            contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE, 0);
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(ProductEntry.TABLE_NAME, null, contentValues);
        if (id < 0) {
            return null;
        } else {
            return ContentUris.withAppendedId(uri, id);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                int numberOfRowsAffected = deleteProduct(selection, selectionArgs);
                if (numberOfRowsAffected > 0)
                    contentResolver.notifyChange(uri, null);
                return numberOfRowsAffected;

            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numberOfRowsAffected = deleteProduct(selection, selectionArgs);
                if (numberOfRowsAffected > 0)
                    contentResolver.notifyChange(uri, null);
                return numberOfRowsAffected;

            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    private int deleteProduct(String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        int numberOfRowsAffected;
        switch (match) {
            case PRODUCTS:
                numberOfRowsAffected = updateProduct(contentValues, selection, selectionArgs);
                if (numberOfRowsAffected > 0) {
                    contentResolver.notifyChange(uri, null);
                }
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numberOfRowsAffected = updateProduct(contentValues, selection, selectionArgs);
                if (numberOfRowsAffected > 0) {
                    contentResolver.notifyChange(uri, null);
                }
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        return numberOfRowsAffected;
    }

    private int updateProduct(ContentValues contentValues, String selection,
                              String[] selectionArgs) {

        if (ProductEntry.isAValidProduct(getContext(), contentValues, ProductEntry.PRODUCT_UPDATE)) {
            throw new IllegalArgumentException("Product has missing or invalid arguments");
        }

        contentValues.remove(ProductEntry.COLUMN_PRODUCT_IMAGE_DUMMY);

        if (contentValues.getAsDouble(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE) == null) {
            contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE, 0);
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
    }
}