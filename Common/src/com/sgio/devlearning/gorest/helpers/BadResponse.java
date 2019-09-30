/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.helpers;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author sgioh
 */
public final class BadResponse {
    @SerializedName("_meta")
    private Metadata metadata;

    @SerializedName("result")
    private Result result;
    
    @Override
    public String toString() {
        return "";
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Result getResult() {
        return this.result;
    }
    
    public class Result {
        private String name;
        private String message;
        private int code;
        private int status;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
