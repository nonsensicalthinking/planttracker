package com.nonsense.planttracker.android.interf;

import android.graphics.Bitmap;

/**
 * Created by Derek Brooks on 3/3/2018.
 */

public interface IImageCache {
    public Bitmap getImage(String path);
}
