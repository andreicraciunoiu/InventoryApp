package com.example.android.electronicsinventory.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.android.electronicsinventory.R;

import java.net.MalformedURLException;
import java.net.URL;

public final class ProductContract {
    static final String CONTENT_AUTHORITY = "com.example.android.electronicsinventory";
    static final String PATH_PRODUCTS = "products";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private ProductContract() {
    }

    public static final class ProductEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_QUANTITY_AVAILABLE = "quantity_available";
        public static final String COLUMN_PRODUCT_SUPPLIER = "supplier_name";
        public static final String COLUMN_PRODUCT_SUPPLIER_PHONE = "resupply_phone";
        public static final String COLUMN_PRODUCT_SUPPLIER_URL = "resupply_url";

        public static final String COLUMN_PRODUCT_IMAGE_DUMMY = "product_image_dummy";
        static final String TABLE_NAME = "electronics";

        static final int PRODUCT_INSERT = 1;
        static final int PRODUCT_UPDATE = 2;

        static final String CONTENT_LIST = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        static final String CONTENT_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        static boolean isAValidProduct(Context context, ContentValues values, int validationMode) {
            Integer id = values.getAsInteger(_ID);
            Resources resources = context.getResources();

            if (id != null) {
                if (isValidNonNegativeInteger(id)) {
                    throw new IllegalArgumentException(resources.getString(R.string.product_validator_message_invalid_id));
                }
            }
            if (isValidNonEmptyString(values, COLUMN_PRODUCT_NAME, validationMode)) {
                throw new IllegalArgumentException(resources.getString(R.string.product_validator_message_invalid_name));
            }

            if (!isValidPositiveDouble(values, validationMode)) {
                throw new IllegalArgumentException(resources.getString(R.string.product_validator_message_invalid_price));
            }

            if (!isValidNonNegativeInteger(values, validationMode)) {
                throw new IllegalArgumentException(resources.getString(R.string.product_validator_message_invalid_quantity));
            }

            if (isValidNonEmptyString(values, COLUMN_PRODUCT_SUPPLIER, validationMode)) {
                throw new IllegalArgumentException(resources.getString(R.string.product_validator_message_invalid_supplier_name));
            }

            if (!isValidUrl(values)) {
                throw new IllegalArgumentException(resources.getString(R.string.product_validator_message_invalid_url));
            }

            if (!isValidBoolean(values, validationMode)) {
                throw new IllegalArgumentException(resources.getString(R.string.product_validator_message_missing_product_image));
            }

            return false;
        }

        static boolean isValidNonNegativeInteger(ContentValues contentValues, int validationMode) {
            if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE)) {
                if (isValidNonNegativeInteger(contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY_AVAILABLE))) {
                    return false;
                }
            } else if (validationMode != PRODUCT_UPDATE) {
                return false;
            }
            return true;
        }

        static boolean isValidNonNegativeInteger(Integer integer) {
            return (integer == null || integer < 0);
        }

        static boolean isValidPositiveDouble(ContentValues contentValues, int validationMode) {
            if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
                if (!isValidPositiveDouble(contentValues.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE))) {
                    return false;
                }
            } else if (validationMode != PRODUCT_UPDATE) {
                return false;
            }
            return true;
        }

        static boolean isValidPositiveDouble(Double aDouble) {
            return (aDouble != null && aDouble > 0);
        }

        static boolean isValidNonEmptyString(ContentValues contentValues, String key, int validationMode) {
            if (contentValues.containsKey(key)) {
                if (!isValidNonEmptyString(contentValues.getAsString(key))) {
                    return true;
                }
            } else if (validationMode != PRODUCT_UPDATE) {
                return true;
            }
            return false;
        }

        static boolean isValidNonEmptyString(String aString) {
            return (aString != null && !aString.isEmpty());
        }

        static boolean isValidBoolean(ContentValues contentValues, int validationMode) {
            if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_IMAGE_DUMMY)) {
                if (!isValidBoolean(contentValues.getAsBoolean(ProductEntry.COLUMN_PRODUCT_IMAGE_DUMMY))) {
                    return false;
                }
            } else if (validationMode != PRODUCT_UPDATE) {
                return false;
            }
            return true;
        }

        static boolean isValidBoolean(Boolean aBoolean) {
            return (aBoolean == (Boolean) true);
        }

        static boolean isValidUrl(ContentValues contentValues) {
            if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL)) {
                if (!isValidUrl(contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL))) {
                    return false;
                }
            }
            return true;
        }

        static boolean isValidUrl(String aString) {
            if (aString == null || aString.isEmpty()) {
                return (true);
            } else {
                try {
                    new URL(aString);
                } catch (MalformedURLException e) {
                    return false;
                }
                return true;
            }
        }
    }
}