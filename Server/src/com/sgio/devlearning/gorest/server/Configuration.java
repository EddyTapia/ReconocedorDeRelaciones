/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.server;

/**
 *
 * @author sgioh
 */
public final class Configuration {
    private final String host;
    private final int port;
    private final String token;

    public Configuration(String ip, int port, String token) {
        this.host = ip;
        this.port = port;
        this.token = token;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getToken() {
        return token;
    }
}
