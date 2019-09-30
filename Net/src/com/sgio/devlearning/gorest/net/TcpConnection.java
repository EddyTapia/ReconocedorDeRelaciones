/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.net;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author sgioh
 */
public class TcpConnection {
    private final List<DisconnectionListener> disconnectionListeners = new ArrayList();
    private final List<BytesReceivedListener> bytesReceivedListeners = new ArrayList();
    
    private final ReentrantLock syncRoot = new ReentrantLock();
    
    private final Socket connectionSocket;
    private final Protocol protocol;
    
    private Boolean connected = false;
    
    protected TcpConnection(Protocol protocol) throws IOException {
        if (protocol == null) {
        }
        
        this.protocol = protocol;
        this.connectionSocket = new Socket();
        this.protocol.setBytesReceivedHandler(data -> this.onBytesReceived(data));
    }
    
    public TcpConnection(Socket connectionSocket, Protocol protocol) {
        if (connectionSocket == null) {
        }
        
        this.connectionSocket = connectionSocket;
        this.protocol = protocol;
        this.protocol.setBytesReceivedHandler(d -> this.onBytesReceived(d));
        this.connected = this.connectionSocket.isConnected();
        
        if (this.isConnected()) {
            this.receiveBytes();
        }
    }
    
    public Boolean isConnected() {
        return this.connected;
    }
    
    protected Socket getConnectionSocket() {
        return this.connectionSocket;
    }
    
    public void addBytesReceivedListener(BytesReceivedListener listener) {
        this.syncRoot.lock();
        try {
            this.bytesReceivedListeners.add(listener);
        } finally {
            this.syncRoot.unlock();
        }
    }
    
    public void addDisconnectionListener(DisconnectionListener listener) {
        this.syncRoot.lock();
        try {
            this.disconnectionListeners.add(listener);
        } finally {
            this.syncRoot.unlock();
        }
    }
    
    private void disconnect() {
        if (this.isConnected()) {
            this.connected = false;
            this.onDisconnected();
        }
    }
    
    public void close() throws IOException {
        this.syncRoot.lock();
        try {
            this.connectionSocket.close();
            this.disconnectionListeners.clear();
            this.bytesReceivedListeners.clear();
        } finally {
            this.connected = false;
            this.syncRoot.unlock();
        }
    }
    
    protected void onDisconnected() {
        System.out.println("From server: Client is disconnected");
        List<DisconnectionListener> listeners = this.cloneDisconnectionListeners();
        CompletableFuture.runAsync(() -> {
            listeners.stream().forEach((listener) -> {
                listener.onDisconnected(this);
            });
        }); 
    }
    
    protected List<BytesReceivedListener> cloneBytesReceivedListeners() {
        this.syncRoot.lock();
        try {
            return new ArrayList(this.bytesReceivedListeners);
        } finally {
            this.syncRoot.unlock();
        }
    }
    
    protected void onBytesReceived(byte[] bytes) {
        List<BytesReceivedListener> listeners = this.cloneBytesReceivedListeners();
        CompletableFuture.runAsync(() -> {
            listeners.stream().forEach((listener) -> {
                listener.onBytesReceived(bytes);
            });
        });
    }
    
    protected List<DisconnectionListener> cloneDisconnectionListeners() {
        this.syncRoot.lock();
        try {
            return new ArrayList(this.disconnectionListeners);
        } finally {
            this.syncRoot.unlock();
        }
    }
    
    public CompletableFuture sendBytes(byte[] bytes) {
        return CompletableFuture.runAsync(() -> {
            byte[] packet = this.protocol.wrapBytes(bytes);
            try {
                this.connectionSocket.getOutputStream().write(packet);
                this.connectionSocket.getOutputStream().flush();
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }
    
    protected void receiveBytes() {
        CompletableFuture.runAsync(() -> {
            
            try {
                byte[] bytes = new byte[this.connectionSocket.getInputStream().available()];
                int i = 0;
                while(i < bytes.length) {
                    i +=  this.connectionSocket.getInputStream().read(bytes, i, bytes.length);
                    if (i < 0) {
                        this.disconnect();
                        break;
                    }
                }
                
                this.protocol.bytesReceived(bytes);
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(t -> {
                this.disconnect();
                return null;
        }).thenRun(() -> {
            if (this.isConnected()) {
                receiveBytes();
            }
        });
    }
}
