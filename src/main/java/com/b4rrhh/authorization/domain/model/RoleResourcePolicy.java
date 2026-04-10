package com.b4rrhh.authorization.domain.model;

public class RoleResourcePolicy {

    private final String roleCode;
    private final String resourceCode;
    private final String permissionProfileCode;
    private final PolicyEffect effect;
    private final PropagationMode propagationMode;
    private final boolean active;

    public RoleResourcePolicy(
            String roleCode,
            String resourceCode,
            String permissionProfileCode,
            PolicyEffect effect,
            PropagationMode propagationMode,
            boolean active
    ) {
        this.roleCode = roleCode;
        this.resourceCode = resourceCode;
        this.permissionProfileCode = permissionProfileCode;
        this.effect = effect;
        this.propagationMode = propagationMode;
        this.active = active;
    }

    public String roleCode() { return roleCode; }
    public String resourceCode() { return resourceCode; }
    public String permissionProfileCode() { return permissionProfileCode; }
    public PolicyEffect effect() { return effect; }
    public PropagationMode propagationMode() { return propagationMode; }
    public boolean active() { return active; }
}
