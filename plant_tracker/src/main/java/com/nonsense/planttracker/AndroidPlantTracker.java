package com.nonsense.planttracker;

import android.app.Application;
import android.content.Context;

import org.acra.*;
import org.acra.annotation.*;

/**
 * Created by Derek Brooks on 2/23/2018.
 */

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "watch.my.grow@gmail.com")
@AcraDialog(resText = R.string.dialog_text,
        resCommentPrompt = R.string.dialog_comment)
public class AndroidPlantTracker extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}