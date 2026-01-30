package com.krouser.backend.shared.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Results in HTTP 404 Not Found response.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceIdentifier;

    public ResourceNotFoundException(String resourceType, String resourceIdentifier) {
        super(String.format("%s not found: %s", resourceType, resourceIdentifier));
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
    }

    public ResourceNotFoundException(String message) {
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
