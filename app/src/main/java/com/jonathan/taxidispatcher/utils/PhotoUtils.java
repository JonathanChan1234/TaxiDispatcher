package com.jonathan.taxidispatcher.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoUtils {
    public static String getStringImage(Bitmap bitmap) {
        String encodedImage = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();
            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return encodedImage;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void storeQRCodeImage(Bitmap qrCodeImage, String platenumber) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        qrCodeImage.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File tmpDir = new File(Environment.getExternalStorageDirectory() + "/" + "taxi");
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        File destination = new File(tmpDir.getAbsolutePath(), platenumber + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
