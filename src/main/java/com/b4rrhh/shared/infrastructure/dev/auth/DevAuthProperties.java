package com.b4rrhh.shared.infrastructure.dev.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.dev-auth")
public class DevAuthProperties {

    private boolean enabled = false;
    private int defaultExpiresInMinutes = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultExpiresInMinutes() {
        return defaultExpiresInMinutes;
    }

    public void setDefaultExpiresInMinutes(int defaultExpiresInMinutes) {
        this.defaultExpiresInMinutes = defaultExpiresInMinutes;
    }
}