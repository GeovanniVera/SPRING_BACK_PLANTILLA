package com.krouser.backend.shared.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Results in HTTP 409 Conflict response.
 */
public class DuplicateResourceException extends RuntimeException {

    private final String resourceType;
    private final String resourceIdentifier;

    public DuplicateResourceException(String resourceType, String resourceIdentifier) {
        super(String.format("%s already exists: %s", resourceType, resourceIdentifier));
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
    }

    public DuplicateResourceException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceIdentifier = "unknown";
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }
}
