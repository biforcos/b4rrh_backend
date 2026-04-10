package com.b4rrhh.authorization.domain.model;

public class SecuredResource {

    private final String code;
    private final String parentCode;
    private final String boundedContextCode;
    private final SecuredResourceKind resourceKind;
    private final String resourceFamilyCode;
    private final String name;
    private final boolean active;

    public SecuredResource(
            String code,
            String parentCode,
            String boundedContextCode,
            SecuredResourceKind resourceKind,
            String resourceFamilyCode,
            String name,
            boolean active
    ) {
        this.code = code;
        this.parentCode = parentCode;
        this.boundedContextCode = boundedContextCode;
        this.resourceKind = resourceKind;
        this.resourceFamilyCode = resourceFamilyCode;
        this.name = name;
        this.active = active;
    }

    public String code() { return code; }
    public String parentCode() { return parentCode; }
    public String boundedContextCode() { return boundedContextCode; }
    public SecuredResourceKind resourceKind() { return resourceKind; }
    public String resourceFamilyCode() { return resourceFamilyCode; }
    public String name() { return name; }
    public boolean active() { return active; }
}
