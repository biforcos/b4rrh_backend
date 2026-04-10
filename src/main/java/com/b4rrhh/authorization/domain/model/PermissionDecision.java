package com.b4rrhh.authorization.domain.model;

public class PermissionDecision {

    public enum Decision { ALLOW, DENY }

    private final Decision decision;
    private final String reason;

    private PermissionDecision(Decision decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public static PermissionDecision allow(String reason) {
        return new PermissionDecision(Decision.ALLOW, reason);
    }

    public static PermissionDecision deny(String reason) {
        return new PermissionDecision(Decision.DENY, reason);
    }

    public boolean isAllowed() { return decision == Decision.ALLOW; }
    public Decision decision() { return decision; }
    public String reason() { return reason; }
}
