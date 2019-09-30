/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.server;

import com.sgio.devlearning.gorest.common.LengthPrefixProtocol;

/**
 *
 * @author sgioh
 */
public final class Protocol extends LengthPrefixProtocol {
    public Protocol() {
    }
    
    private void onBytesReceived(byte[] bytes) {
        String command = new String(bytes);
        
        switch(command) {
            case "restore" :
                
                break;
        }
    }
}
