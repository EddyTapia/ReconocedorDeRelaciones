/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.net;

/**
 *
 * @author sgioh
 */
public interface ClientConnectionListener {
    void onClientConnected(TcpConnection connection);
}
