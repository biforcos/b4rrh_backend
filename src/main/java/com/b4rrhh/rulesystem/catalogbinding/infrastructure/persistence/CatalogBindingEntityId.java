package com.b4rrhh.rulesystem.catalogbinding.infrastructure.persistence;

import java.io.Serializable;
import java.util.Objects;

public class CatalogBindingEntityId implements Serializable {

    private String resourceCode;
    private String fieldCode;

    public CatalogBindingEntityId() {
    }

    public CatalogBindingEntityId(String resourceCode, String fieldCode) {
        this.resourceCode = resourceCode;
        this.fieldCode = fieldCode;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CatalogBindingEntityId that)) {
            return false;
        }
        return Objects.equals(resourceCode, that.resourceCode)
                && Objects.equals(fieldCode, that.fieldCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceCode, fieldCode);
    }
}
