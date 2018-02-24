package com.nonsense.planttracker.android;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.nonsense.planttracker.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Derek Brooks on 2/4/2018.
 */

public class Utility {

    public static Dialog displayOperationInProgressDialog(Context c, String title)    {
        Dialog operationInProgressDialog = new Dialog(c);
        operationInProgressDialog.setContentView(R.layout.dialog_op_in_progress);
        operationInProgressDialog.setCanceledOnTouchOutside(false);

        if (title != null)  {
            operationInProgressDialog.setTitle(title);
        }

        operationInProgressDialog.show();

        return operationInProgressDialog;
    }


    public static void copyUriToLocation(InputStream is, String destination)
            throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(destination);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] buf = new byte[1024];
        int readCount = 0;
        while((readCount = is.read(buf, 0, 1024)) != -1)   {
            bos.write(buf, 0, readCount);
        }

        bos.close();
        is.close();
    }


    // https://developer.android.com/topic/performance/graphics/load-bitmap.html
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                            int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(File file, int reqWidth,
                                                         int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

}
