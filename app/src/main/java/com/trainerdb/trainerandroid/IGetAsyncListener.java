package com.trainerdb.trainerandroid;

/**
 * Created by dcotrim on 20/09/2016.
 */
public interface IGetAsyncListener<T> {
    void onDataGet(boolean success, T data);
}
