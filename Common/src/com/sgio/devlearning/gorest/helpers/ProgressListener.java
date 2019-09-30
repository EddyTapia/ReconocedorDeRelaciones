/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.helpers;

/**
 *
 * @author sgioh
 */
public interface ProgressListener {
    void progressChanged(float deltaProgress);
    
    void operationFailed(String reason);
}
