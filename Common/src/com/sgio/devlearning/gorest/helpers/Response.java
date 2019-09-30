/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.helpers;

import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author sgioh
 * @param <T>
 */
public class Response<T> {
    @SerializedName("_meta")
    private Metadata metadata;

    @SerializedName("result")
    private T[] data;

    public Response() { }

    public static <T> Response<T> empty(Class<T> clazz) {
        
        Response<T> r = new Response();
        
        r.data = (T[])Array.newInstance(clazz, 0);
        
        return r;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Collection<T> getData() {
        return Arrays.asList(data);
    }
}
