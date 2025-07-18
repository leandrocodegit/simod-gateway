package br.sincroled.gateway;

import org.springframework.http.HttpMethod;

import java.util.List;

public class EndPonitValid {
    private List<String> roles;
    private boolean allMethods;
    private List<HttpMethod> methods;

    public EndPonitValid(List<String> roles, boolean allMethods, List<HttpMethod> methods) {
        this.roles = roles;
        this.allMethods = allMethods;
        this.methods = methods;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isAllMethods() {
        return allMethods;
    }

    public void setAllMethods(boolean allMethods) {
        this.allMethods = allMethods;
    }

    public List<HttpMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<HttpMethod> methods) {
        this.methods = methods;
    }
}
