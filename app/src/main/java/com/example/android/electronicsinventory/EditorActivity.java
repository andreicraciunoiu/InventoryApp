package com.example.android.electronicsinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.electronicsinventory.data.ProductContract.ProductEntry;
import com.example.android.electronicsinventory.data.ProductFileHelper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final String PRODUCT_TEMPORARY_IMAGE_STATE = "has_temporary_image";
    private static final int SINGLE_PRODUCT_LOADER_ID = 2;
    private Uri currentProductUri = null;
    private boolean productHasChanged = false;
    private ImageView productImage;
    private EditText nameText;
    private EditText priceText;
    private EditText quantityAvailableText;
    private EditText supplierNameText;
    private EditText supplierPhoneText;
    private EditText supplierUrlText;
    private File productTemporaryImage = null;
    private boolean hasTemporaryImage = false;
    private boolean productHasImage = false;

    private final View.OnTouchListener onImageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Intent imageIntent = ImagePick.getImageIntent(getApplicationContext());
            startActivityForResult(imageIntent, PICK_IMAGE_REQUEST_CODE);
            return false;
        }
    };

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            productHasChanged = true;
            return false;
        }
    };

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.increase_quantity:
                    increaseQuantity();
                    break;
                case R.id.decrease_quantity:
                    decreaseQuantity();
                    break;
                case R.id.call_supplier_phone_button:
                    callSupplier();
                    break;
                case R.id.open_supplier_url_button:
                    openUrl();
                    break;
                default: //no default statement in this case
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        LoaderManager manager = getLoaderManager();

        productImage = (ImageView) findViewById(R.id.edit_product_image);
        nameText = (EditText) findViewById(R.id.edit_product_name);
        priceText = (EditText) findViewById(R.id.edit_product_price);
        quantityAvailableText = (EditText) findViewById(R.id.edit_quantity_in_stock);
        supplierNameText = (EditText) findViewById(R.id.edit_supplier_name);
        supplierPhoneText = (EditText) findViewById(R.id.edit_supplier_phone);
        supplierUrlText = (EditText) findViewById(R.id.edit_supplier_url);

        ImageButton increaseQuantityButton = (ImageButton) findViewById(R.id.increase_quantity);
        ImageButton decreaseQuantityButton = (ImageButton) findViewById(R.id.decrease_quantity);
        ImageButton supplierPhone = (ImageButton) findViewById(R.id.call_supplier_phone_button);
        ImageButton supplierUrl = (ImageButton) findViewById(R.id.open_supplier_url_button);

        productImage.setOnTouchListener(onImageTouchListener);

        nameText.setOnTouchListener(onTouchListener);
        priceText.setOnTouchListener(onTouchListener);
        quantityAvailableText.setOnTouchListener(onTouchListener);
        supplierNameText.setOnTouchListener(onTouchListener);
        supplierPhoneText.setOnTouchListener(onTouchListener);
        supplierUrlText.setOnTouchListener(onTouchListener);

        increaseQuantityButton.setOnClickListener(onClickListener);
        decreaseQuantityButton.setOnClickListener(onClickListener);
        supplierPhone.setOnClickListener(onClickListener);
        supplierUrl.setOnClickListener(onClickListener);

        Intent intent = getIntent();
        Uri productUri = intent.getData();

        if (productUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            increaseQuantityButton.setVisibility(View.GONE);
            decreaseQuantityButton.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            currentProductUri = productUri;

            File productImageFile = ProductFileHelper.getProductImageFile(this, currentProductUri);
            if (productImageFile != null) {
                productHasImage = true;
                productImage.setImageURI(Uri.fromFile(productImageFile));
            }
            manager.initLoader(SINGLE_PRODUCT_LOADER_ID, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PRODUCT_TEMPORARY_IMAGE_STATE, hasTemporaryImage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(PRODUCT_TEMPORARY_IMAGE_STATE)) {
            hasTemporaryImage = savedInstanceState.getBoolean(PRODUCT_TEMPORARY_IMAGE_STATE);

            if (hasTemporaryImage) {
                productTemporaryImage = ProductFileHelper.getProductTemporaryImageFile(this);
                if (productTemporaryImage == null) {
                    hasTemporaryImage = false;
                    Toast.makeText(this, getString(R.string.temporary_image_loading_failed), Toast.LENGTH_LONG).show();
                } else {
                    productImage.setImageURI(Uri.fromFile(productTemporaryImage));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean deleteProduct() {
        if (currentProductUri == null) {
            return false;
        }

        String toastMessage;
        boolean result = false;

        int rowsAffected = getContentResolver().delete(currentProductUri, null, null);

        if (rowsAffected > 0) {
            toastMessage = getResources().getString(R.string.editor_delete_product_successful);
            ProductFileHelper.deleteProductImage(this, currentProductUri);
            result = true;
        } else {
            toastMessage = getResources().getString(R.string.editor_delete_product_failed);
        }

        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        if (result) {
            finish();
        }
        return result;
    }

    private boolean saveProduct() {
        ContentValues insertData = new ContentValues();

        insertData.put(ProductEntry.COLUMN_PRODUCT_IMAGE_DUMMY, (productHasImage || hasTemporaryImage));
        insertData.put(ProductEntry.COLUMN_PRODUCT_NAME, nameText.getText().toString().trim());
        insertData.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceText.getText().toString().trim());
        insertData.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE, quantityAvailableText.getText().toString().trim());
        insertData.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierNameText.getText().toString().trim());
        insertData.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhoneText.getText().toString().trim());
        insertData.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL, supplierUrlText.getText().toString().trim());

        boolean result = false;
        String toastMessage;
        try {
            if (currentProductUri == null) {
                Uri resultUri = getContentResolver().insert(ProductEntry.CONTENT_URI, insertData);
                if (resultUri == null) {
                    toastMessage = getResources().getString(R.string.db_insert_product_failed);
                } else {
                    toastMessage = getResources().getString(R.string.db_insert_product_succeeded);
                    currentProductUri = resultUri;
                    result = true;
                }
            } else {
                int rowsAffected = getContentResolver().update(currentProductUri, insertData, null, null);
                if (rowsAffected > 0) {
                    toastMessage = getResources().getString(R.string.db_update_product_updated);
                    result = true;
                } else {
                    toastMessage = getResources().getString(R.string.db_update_product_update_failed);
                }
            }
        } catch (IllegalArgumentException e) {
            toastMessage = e.getMessage();
        }

        if (hasTemporaryImage && result) {
            if (!ProductFileHelper.saveTemporaryFileToProductImageFile(this, currentProductUri)) {
                toastMessage = getResources().getString(R.string.failed_to_save_product_image);
                result = false;
            }
            if (result) {
                hasTemporaryImage = false;
            }
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveProduct()) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.home:
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButton = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButton);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        return new CursorLoader(this, currentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        if (cursor == null) {
            return;
        } else if (!cursor.moveToNext()) {
            return;
        }

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityAvailableColumnIdex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE);
        int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        int supplierUrlColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL);

        nameText.setText(cursor.getString(nameColumnIndex));
        priceText.setText(cursor.getString(priceColumnIndex));
        quantityAvailableText.setText(cursor.getString(quantityAvailableColumnIdex));
        supplierNameText.setText(cursor.getString(supplierColumnIndex));
        supplierPhoneText.setText(cursor.getString(supplierPhoneColumnIndex));
        supplierUrlText.setText(cursor.getString(supplierUrlColumnIndex));


    }

    @Override
    public void onLoaderReset(Loader loader) {
        nameText.setText("");
        priceText.setText("");
        quantityAvailableText.setText("");
        supplierNameText.setText("");
        supplierPhoneText.setText("");
        supplierUrlText.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_REQUEST_CODE:
                saveAndSetTemporaryProductImage(ImagePick.getImage(this, resultCode, data));
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void saveAndSetTemporaryProductImage(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, getResources().getString(R.string.pick_image_intent_returned_null), Toast.LENGTH_LONG).show();
        } else {
            productTemporaryImage = ProductFileHelper.putProductTemporaryImageFile(this, bitmap);

            if (productTemporaryImage == null) {
                hasTemporaryImage = false;
                Toast.makeText(this,
                        getResources().getString(R.string.failed_to_save_temporary_product_image),
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                productHasChanged = true;
                hasTemporaryImage = true;
                productImage.setImageURI(Uri.fromFile(productTemporaryImage));
            }
        }
    }

    private void increaseQuantity() {
        productHasChanged = true;
        int quantity = Integer.parseInt(quantityAvailableText.getText().toString().trim());
        quantityAvailableText.setText(String.valueOf(quantity + 1));
    }

    private void decreaseQuantity() {
        int quantity = Integer.parseInt(quantityAvailableText.getText().toString().trim());
        if (quantity > 0) {
            productHasChanged = true;
            quantityAvailableText.setText(String.valueOf(quantity - 1));
        }
    }

    private void callSupplier() {
        String phoneNumber = supplierPhoneText.getText().toString();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.message_please_set_phone_number_first), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);

        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, getResources().getString(R.string.error_message_no_activity_to_place_call), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, getResources().getString(R.string.error_message_no_permission_to_place_call), Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrl() {
        String urlString = supplierUrlText.getText().toString();
        if (urlString.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.message_please_set_url_first), Toast.LENGTH_SHORT).show();
            return;
        }

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Toast.makeText(this, getResources().getString(R.string.error_message_malformed_url), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url.toString()));
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, getResources().getString(R.string.error_message_no_activity_to_open_url), Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }
}