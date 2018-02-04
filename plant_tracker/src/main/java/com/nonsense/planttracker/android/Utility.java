package com.nonsense.planttracker.android;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.nonsense.planttracker.R;

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

}
