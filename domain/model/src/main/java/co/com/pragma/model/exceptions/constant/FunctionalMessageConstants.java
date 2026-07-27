package co.com.pragma.model.exceptions.constant;

public final class FunctionalMessageConstants {

    private FunctionalMessageConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String BUSINESS_VALIDATION_FAILED = "Business validation failed";
    public static final String BOOTCAMP_ALREADY_EXISTS = "Bootcamp name already exists";
    public static final String CAPABILITIES_NOT_FOUND = "The following capability ids do not exist: %s";
    public static final String BOOTCAMP_NOT_FOUND = "Bootcamp not found";
}
