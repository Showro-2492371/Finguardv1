package org.cts.adm.finguard.Jwt;

public class JwtUser {
    private final String username;
    private final Long customerId;

    public JwtUser(String username, Long customerId) {
        this.username = username;
        this.customerId = customerId;
    }

    public String getUsername() {
        return username;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
