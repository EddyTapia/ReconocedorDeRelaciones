/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.client;

import com.sgio.devlearning.gorest.common.LengthPrefixProtocol;
import com.sgio.devlearning.gorest.net.DisconnectionListener;
import com.sgio.devlearning.gorest.net.BytesReceivedListener;
import com.sgio.devlearning.gorest.net.TcpClient;
import com.sgio.devlearning.gorest.net.TcpConnection;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author sgioh
 */
public final class Restorer implements DisconnectionListener, BytesReceivedListener {
    private final TcpClient client;
    
    public Restorer(Configuration configuration) throws IOException {
        client = new TcpClient(new LengthPrefixProtocol());
        client.addDisconnectionListener(this);
        client.addBytesReceivedListener(this);
        client.connect(configuration.getHost(), configuration.getPort())
              .thenRun(() -> {
                  System.out.println("Connected to server!");
              });
    }
    
    public void restore() {
        this.client.sendBytes("Hola, servidor.".getBytes())
                   .thenRun(() -> System.out.println("Message sent!"));
    }
    
    public void close() throws IOException {
        this.client.close();
    }

    @Override
    public void onDisconnected(TcpConnection connection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onBytesReceived(byte[] buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
