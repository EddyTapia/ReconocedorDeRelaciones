/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.common;

import com.sgio.devlearning.gorest.net.Protocol;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 *
 * @author sgioh
 */
public class LengthPrefixProtocol implements Protocol {
    private static final int maxMessageSize = 2048;
    
    private final byte[] lengthBuffer = new byte[4];
    
    private byte[] dataBuffer = null;
    
    private int bytesReceived = 0;
    
    private Consumer<byte[]> messageReceived;
    
    public LengthPrefixProtocol() { }
    
    @Override
    public void bytesReceived(byte[] bytes) throws IOException {
        int bytesRead = 0;
        
        while (bytesRead != bytes.length) {
            int bytesAvailable = bytes.length - bytesRead;
            
            if (this.dataBuffer != null) {
                int bytesRequested = this.dataBuffer.length - this.bytesReceived;
                int bytesTransferred = Math.min(bytesRequested, bytesAvailable);
                
                System.arraycopy(bytes, bytesRead, this.dataBuffer, this.bytesReceived, bytesTransferred);
                
                bytesRead += bytesTransferred;
                this.bytesReceived = bytesTransferred;
                
                if (this.bytesReceived == this.dataBuffer.length) {
                    if (this.messageReceived != null) {
                        this.messageReceived.accept(this.dataBuffer);
                        
                        this.dataBuffer = null;
                        this.bytesReceived = 0;
                    }
                }
            } else {
                int bytesRequested = this.lengthBuffer.length - this.bytesReceived;
                int bytesTransferred = Math.min(bytesRequested, bytesAvailable);
                
                System.arraycopy(bytes, bytesRead, this.lengthBuffer, this.bytesReceived, bytesTransferred);
                
                bytesRead += bytesTransferred;
                this.bytesReceived += bytesTransferred;
                
                if (this.bytesReceived == (Integer.SIZE/8)) {
                    int length = ByteBuffer.wrap(this.lengthBuffer).getInt();
                    
                    if (length < 0 || length > maxMessageSize) {
                        throw new IOException();
                    }
                    
                    if (length == 0) {
                        this.bytesReceived = 0;
                        if (this.messageReceived != null) {
                            this.messageReceived.accept(new byte[0]);
                        }
                    } else {
                        this.dataBuffer = new byte[length];
                        this.bytesReceived = 0;
                    }
                }
            }
        }
    }

    @Override
    public byte[] wrapBytes(byte[] bytes) {
        return ByteBuffer.allocate(4 + bytes.length)
                         .putInt(bytes.length)
                         .put(bytes)
                         .array();
    }
    
    @Override
    public byte[] wrapKeepaliveMessage() {
        return ByteBuffer.allocate(4)
                         .putInt(0)
                         .array();
    }

    @Override
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    @Override
    public void setBytesReceivedHandler(Consumer<byte[]> handler) {
        this.messageReceived = handler;
    }
}
