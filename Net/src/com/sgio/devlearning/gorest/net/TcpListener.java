package com.sgio.devlearning.gorest.net;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.locks.ReentrantLock;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sgioh
 */
public final class TcpListener implements DisconnectionListener {  
    private final ReentrantLock syncRoot;
    private final ServerSocket serverSocket;
    private final List<TcpConnection> connections;
    private final List<ClientConnectionListener> listeners;
        
    private Boolean listening = false;
            
    public TcpListener(String host, int port, Protocol protocol) throws IOException {
        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(new InetSocketAddress(host, port), 5);
        
        this.syncRoot = new ReentrantLock();
        
        this.listeners = new ArrayList();
        this.connections = new ArrayList();
        
        this.listen(protocol);
    }
    
    public Boolean isListening() {
        return this.listening;
    }
    
    public int getConnectionCount() {
        return this.connections.size();
    }
    
    public void addClientConnectionListener(ClientConnectionListener listener) {
        this.syncRoot.lock();
        try {
            this.listeners.add(listener);
        } finally {
            this.syncRoot.unlock();
        }
    }
    
    public void close() throws IOException {
        this.syncRoot.lock();
        try {
            if (this.isListening()) {
                this.serverSocket.close();
            }
        } finally {
            this.listening = false;
            this.syncRoot.unlock();
        }
    }
    
    private void addConnection(TcpConnection connection) {
        if (connection == null) {
        }
        
        this.connections.add(connection);
        
        CompletableFuture.runAsync(() -> {
            this.listeners.stream().forEach((listener) -> {
                listener.onClientConnected(connection);
            });
        });
    }
    
    private void removeConnection(TcpConnection connection) {
        this.syncRoot.lock();
        try {
            this.connections.remove(connection);
        } finally {
            this.syncRoot.unlock();
        }
    }
    
    private void listen(Protocol protocol) {
        this.listening = true;
        CompletableFuture.runAsync(() -> {
            try {
                Socket connectionSocket = serverSocket.accept();
                
                if (connectionSocket != null) {
                    addConnection(new TcpConnection(connectionSocket, protocol));
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        }).exceptionally(r -> { 
            try {
                close();
            } catch (IOException ex) { }
            return null; 
            })
          .thenRun(() -> {
              if (this.isListening()) {
                  listen(protocol);
              }
          });
    }

    @Override
    public void onDisconnected(TcpConnection connection) {
        this.removeConnection(connection);
    }
}
