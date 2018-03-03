package com.nonsense.planttracker.android.interf;

/**
 * Created by Derek Brooks on 3/3/2018.
 */

public interface IAction<T> {
    public void exec(T o);
}
