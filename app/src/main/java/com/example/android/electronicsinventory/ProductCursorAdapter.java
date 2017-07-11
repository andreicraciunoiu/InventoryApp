package com.example.android.electronicsinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.electronicsinventory.data.ProductContract;
import com.example.android.electronicsinventory.data.ProductContract.ProductEntry;
import com.example.android.electronicsinventory.data.ProductFileHelper;

import java.io.File;
import java.text.NumberFormat;

class ProductCursorAdapter extends CursorAdapter {
    ProductCursorAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.product_image);
        TextView nameText = (TextView) view.findViewById(R.id.product_name);
        TextView priceText = (TextView) view.findViewById(R.id.product_price);
        TextView quantityAvailableText = (TextView) view.findViewById(R.id.product_quantity_available);
        final Button sellButton = (Button) view.findViewById(R.id.product_sell_button);
        TextView inStockText = (TextView) view.findViewById(R.id.in_stock_text);

        int idColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityAvailableColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE);

        final long productId = cursor.getLong(idColumnIndex);
        final int quantityAvailable = cursor.getInt(quantityAvailableColumnIndex);

        double price = cursor.getDouble(priceColumnIndex);

        File productImageFile = ProductFileHelper.getProductImageFile(context, productId);
        if (productImageFile != null) {
            imageView.setImageDrawable(null);
            imageView.setImageURI(Uri.fromFile(productImageFile));
        }

        nameText.setText(cursor.getString(nameColumnIndex));
        priceText.setText(NumberFormat.getCurrencyInstance().format(price));

        if (quantityAvailable < 1) {
            sellButton.setEnabled(false);
            inStockText.setText(context.getResources().getString(R.string.out_of_stock));
            quantityAvailableText.setVisibility(View.GONE);
        } else {
            sellButton.setEnabled(true);
            quantityAvailableText.setText(cursor.getString(quantityAvailableColumnIndex));
            inStockText.setText(context.getResources().getString(R.string.in_stock));
            quantityAvailableText.setVisibility(View.VISIBLE);
            sellButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sellItem(context, productId, quantityAvailable);
                }
            });
        }
    }

    private void sellItem(Context context, long productId, int quantityAvailable) {

        Resources resources = context.getResources();

        if (quantityAvailable < 1) {
            Toast.makeText(context, resources.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
            return;
        }

        Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
        ContentValues updateData = new ContentValues();
        updateData.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE, quantityAvailable - 1);

        String toastMessage;
        try {
            int rowsAffected = context.getContentResolver().update(productUri, updateData, null, null);
            if (rowsAffected > 0) {
                toastMessage = resources.getString(R.string.product_sold_successfully);
            } else {
                toastMessage = resources.getString(R.string.db_update_product_update_failed);
            }
        } catch (IllegalArgumentException e) {
            toastMessage = e.getMessage();
        }

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }
}