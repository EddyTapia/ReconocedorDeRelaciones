/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.client;

/**
 *
 * @author sgioh
 */
public final class Configuration {
    private final String host;
    private final int port;

    public Configuration(String ip, int port) {
        this.host = ip;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
