package org.cts.adm.finguard.CustomerOnboarding.Dto;

public class LoginRequest {

    private String name;
    private String password;

    public LoginRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
