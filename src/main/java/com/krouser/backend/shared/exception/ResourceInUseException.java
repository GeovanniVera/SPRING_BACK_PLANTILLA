package com.krouser.backend.shared.exception;

/**
 * Exception thrown when attempting to delete a resource that is currently in
 * use.
 * Results in HTTP 409 Conflict response.
 */
public class ResourceInUseException extends RuntimeException {

    private final String resourceType;
    private final String resourceIdentifier;
    private final String usedBy;

    public ResourceInUseException(String resourceType, String resourceIdentifier, String usedBy) {
        super(String.format("Cannot delete %s '%s': currently in use by %s",
                resourceType, resourceIdentifier, usedBy));
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
        this.usedBy = usedBy;
    }

    public ResourceInUseException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceIdentifier = "unknown";
        this.usedBy = "unknown";
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public String getUsedBy() {
        return usedBy;
    }
}
