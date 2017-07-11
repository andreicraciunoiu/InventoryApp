package com.example.android.electronicsinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.electronicsinventory.data.ProductContract.ProductEntry;
import com.example.android.electronicsinventory.data.ProductFileHelper;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_LIST_LOADER_ID = 1;
    private ProductCursorAdapter productCursorAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LoaderManager manager = getLoaderManager();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CatalogActivity.this, EditorActivity.class));
            }
        });

        ListView electronicsList = (ListView) findViewById(R.id.product_list_view);
        View emptyView = findViewById(R.id.empty_view);
        electronicsList.setEmptyView(emptyView);

        productCursorAdapter = new ProductCursorAdapter(this);
        electronicsList.setAdapter(productCursorAdapter);
        manager.initLoader(PRODUCT_LIST_LOADER_ID, null, this);

        electronicsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_all_entries) {
            deleteAllProducts();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllProducts() {
        getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        ProductFileHelper.deleteAllProductImages(this);
        Toast.makeText(this, getString(R.string.db_delete_all_products), Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL
        };
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        productCursorAdapter.changeCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productCursorAdapter.changeCursor(null);

    }
}