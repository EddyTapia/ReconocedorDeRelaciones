/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.net;

import java.io.IOException;
import java.util.function.Consumer;

/**
 *
 * @author sgioh
 */
public interface Protocol {
    int getMaxMessageSize();
    
    void setBytesReceivedHandler(Consumer<byte[]> handler);
    
    void bytesReceived(byte [] bytes) throws IOException;
    
    byte[] wrapBytes(byte[] bytes);
    
    byte[] wrapKeepaliveMessage();
}
