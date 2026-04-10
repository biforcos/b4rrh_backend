package com.b4rrhh.authorization.domain.model;

import java.util.Set;

public class PermissionProfile {

    private final String code;
    private final String name;
    private final Set<String> actionCodes;

    public PermissionProfile(String code, String name, Set<String> actionCodes) {
        this.code = code;
        this.name = name;
        this.actionCodes = Set.copyOf(actionCodes);
    }

    public boolean containsAction(String actionCode) {
        return actionCodes.contains(actionCode.toUpperCase());
    }

    public String code() { return code; }
    public String name() { return name; }
    public Set<String> actionCodes() { return actionCodes; }
}
