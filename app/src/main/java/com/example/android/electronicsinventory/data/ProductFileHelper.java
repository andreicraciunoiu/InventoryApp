package com.example.android.electronicsinventory.data;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ProductFileHelper {
    private static final String PRODUCT_IMAGE_DIRECTORY = "product_images";
    private static final String IMAGE_FILE_EXTENSION = ".png";
    private static final Bitmap.CompressFormat BITMAP_COMPRESSION_FORMAT = Bitmap.CompressFormat.PNG;
    private static final String EDITOR_TEMPORARY_IMAGE_FILE_NAME = "editor_activity_temporary_image_file.png";

    private ProductFileHelper() {
    }

    private static String getProductImageFileName(long productId) {
        return PRODUCT_IMAGE_DIRECTORY + "/" + productId + IMAGE_FILE_EXTENSION;
    }

    private static boolean writeBitmapToFile(File imageFile, Bitmap bitmap) {
        if (!imageFile.canWrite()) {
            return false;
        }

        FileOutputStream outputStream = null;
        boolean success = false;

        try {
            outputStream = new FileOutputStream(imageFile.getAbsoluteFile());
            bitmap.compress(BITMAP_COMPRESSION_FORMAT, 100, outputStream);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                success = false;
            }
        }
        return success;
    }

    public static File getProductImageFile(Context context, Uri productUri) {
        long productId = ContentUris.parseId(productUri);
        return getProductImageFile(context, productId, false);
    }

    private static File getProductImageFile(Context context, Uri productUri, boolean create) {
        long productId = ContentUris.parseId(productUri);
        return getProductImageFile(context, productId, create);
    }

    public static File getProductImageFile(Context context, long productId) {
        return getProductImageFile(context, productId, false);
    }

    @Nullable
    private static File getProductImageFile(Context context, long productId, boolean create) {

        String filename = getProductImageFileName(productId);
        File imageFile = new File(context.getFilesDir(), filename);
        File imageDirFile = imageFile.getParentFile();

        if (!checkDirectory(imageDirFile, create)) {
            return null;
        }

        if (!checkFile(imageFile, create)) {
            return null;
        }
        return imageFile;
    }

    @Nullable
    public static File putProductTemporaryImageFile(Context context, Bitmap bitmap) {
        File temporaryImageFile = new File(context.getFilesDir(), EDITOR_TEMPORARY_IMAGE_FILE_NAME);

        if (checkFile(temporaryImageFile, true) && writeBitmapToFile(temporaryImageFile, bitmap)) {
            return temporaryImageFile;
        } else {
            return null;
        }
    }

    public static File getProductTemporaryImageFile(Context context) {
        return getProductTemporaryImageFile(context, false);
    }

    @Nullable
    private static File getProductTemporaryImageFile(Context context, boolean create) {
        File temporaryImageFile = new File(context.getFilesDir(), EDITOR_TEMPORARY_IMAGE_FILE_NAME);

        if (!checkFile(temporaryImageFile, create)) {
            return null;
        }
        return temporaryImageFile;
    }

    public static boolean saveTemporaryFileToProductImageFile(Context context, Uri productUri) {
        File temporaryFile = getProductTemporaryImageFile(context);
        File productImageFile = getProductImageFile(context, productUri, true);

        if (temporaryFile == null) {
            return false;
        }

        if (productImageFile == null) {
            return false;
        }

        try {
            copyFile(temporaryFile, productImageFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void copyFile(File sourceFile, File destinationFile) throws IOException {
        try (InputStream in = new FileInputStream(sourceFile)) {
            try (OutputStream out = new FileOutputStream(destinationFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    private static boolean checkDirectory(File directory, boolean create) {

        if (!directory.exists()) {
            if (create) {
                if (!directory.mkdirs()) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFile(File file, boolean create) {
        if (!file.exists()) {
            if (create) {
                try {
                    if (!file.createNewFile()) {
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static void deleteProductImage(Context context, Uri productUri) {
        long productId = ContentUris.parseId(productUri);
        deleteProductImage(context, productId);
    }

    private static boolean deleteProductImage(Context context, long productId) {
        File productImageFile = getProductImageFile(context, productId);
        return productImageFile != null && productImageFile.delete();
    }

    public static boolean deleteAllProductImages(Context context) {
        File imageDirFile = new File(context.getFilesDir(), PRODUCT_IMAGE_DIRECTORY);

        if (imageDirFile.isDirectory()) {
            boolean success = true;
            for (File imageFile : imageDirFile.listFiles()) {
                if (!imageFile.delete()) {
                    success = false;
                }
            }
            return success;
        } else {
            return false;
        }
    }
}