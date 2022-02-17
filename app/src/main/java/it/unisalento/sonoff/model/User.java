package it.unisalento.sonoff.model;

import java.io.Serializable;

public class User implements Serializable {
    String username;
    String role;
    String token;
    String refreshTken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshTken() {
        return refreshTken;
    }

    public void setRefreshTken(String refreshTken) {
        this.refreshTken = refreshTken;
    }
}
