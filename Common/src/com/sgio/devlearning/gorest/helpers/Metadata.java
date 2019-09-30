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
public final class Metadata {
    private Boolean success;
    private int code;
    private String message;
    private int totalCount;
    private int pageCount;
    private int currentPage;
    private int perPage;
    
    @SerializedName("rateLimit")
    private RateLimit rateLimit;
    
    @Override
    public String toString() {
        return "success: " + success + 
                ", code: " + code + 
                ", message: " + message + 
                ", totalCount: " + totalCount + 
                ", pageCount: " + pageCount +
                ", currentPage: " + currentPage + 
                ", perPage: " + perPage;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean succes) {
        this.success = succes;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }
}
