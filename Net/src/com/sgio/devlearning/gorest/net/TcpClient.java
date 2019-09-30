/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 *
 * @author sgioh
 */
public final class TcpClient extends TcpConnection {
    public TcpClient(Protocol protocol) throws IOException {
        super(protocol);
    }
    
    public CompletableFuture connect(String host, int port) {
        return CompletableFuture.runAsync(() -> {
            try {
                SocketAddress endPoint = new InetSocketAddress(host, port);
                
                super.getConnectionSocket().connect(endPoint);
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).thenRun(() -> {
            if (super.isConnected()) {
                super.receiveBytes();
            }
        });
    }
}
