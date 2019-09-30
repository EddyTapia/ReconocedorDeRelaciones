/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.server;

import com.sgio.devlearning.gorest.common.LengthPrefixProtocol;
import com.sgio.devlearning.gorest.net.ClientConnectionListener;
import com.sgio.devlearning.gorest.net.DisconnectionListener;
import com.sgio.devlearning.gorest.net.BytesReceivedListener;
import com.sgio.devlearning.gorest.net.TcpConnection;
import com.sgio.devlearning.gorest.net.TcpListener;
import java.io.IOException;

/**
 *
 * @author sgioh
 */
public class TcpServer implements ClientConnectionListener, DisconnectionListener, BytesReceivedListener {
    private final TcpListener server;
    
    public TcpServer(Configuration configuration) throws IOException {
        server = new TcpListener(
                configuration.getHost(), configuration.getPort(), new LengthPrefixProtocol());
        
        server.addClientConnectionListener(this);
    }
    
    public void close() throws IOException {
        this.server.close();
    }

    @Override
    public void onClientConnected(TcpConnection connection) {
        connection.addDisconnectionListener(this);
        connection.addBytesReceivedListener(this);
        
        System.out.println("New connection received!");
    }

    @Override
    public void onDisconnected(TcpConnection connection) {
        System.out.println("Client disconnected!");
    }

    @Override
    public void onBytesReceived(byte[] buffer) {
        System.out.println("Message received: " + new String(buffer));
    }
}
