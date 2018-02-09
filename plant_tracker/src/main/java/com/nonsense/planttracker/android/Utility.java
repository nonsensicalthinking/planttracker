package com.nonsense.planttracker.android;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.nonsense.planttracker.R;

import java.io.BufferedOutputStream;
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
}
