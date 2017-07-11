package com.example.android.electronicsinventory;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

class ImagePick {
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;
    private static final String TEMPORARY_NAME = "tempImage";

    static Intent getImageIntent(Context context) {
        Intent pickerIntent = null;

        List<Intent> intentList = new ArrayList<>();
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("return-data", true);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getFile(context)));
        intentList = addIntentToList(context, intentList, pickIntent);
        intentList = addIntentToList(context, intentList, takePictureIntent);

        if (intentList.size() > 0) {
            pickerIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), context.getString(R.string.pick_image_intent_text));
            pickerIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }
        return pickerIntent;
    }

    private static List<Intent> addIntentToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : infos) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent target = new Intent(intent);
            target.setPackage(packageName);
            list.add(target);
        }
        return list;
    }

    static Bitmap getImage(Context context, int result, Intent imageIntent) {
        Bitmap bitmap = null;
        File imageFile = getFile(context);
        if (result == Activity.RESULT_OK) {
            Uri selectedImage;
            boolean isCamera = (imageIntent == null || imageIntent.getData() == null || imageIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {
                selectedImage = Uri.fromFile(imageFile);
            } else {
                selectedImage = imageIntent.getData();
            }
            bitmap = getImageResized(context, selectedImage);
            int rotation = getRotation(context, selectedImage, isCamera);
            bitmap = rotate(bitmap, rotation);
        }
        return bitmap;
    }

    private static File getFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), TEMPORARY_NAME);
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fileDescriptor != null;
        return BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
    }

    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm;
        int[] sampleSizes = new int[]{5, 3, 2, 1};
        int i = 0;
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
            i++;
        } while (bm.getWidth() < DEFAULT_MIN_WIDTH_QUALITY && i < sampleSizes.length);
        return bm;
    }

    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }
        return rotation;
    }

    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {

            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        return bm;
    }
}