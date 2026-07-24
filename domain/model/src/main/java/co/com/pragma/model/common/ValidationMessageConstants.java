package co.com.pragma.model.common;

public final class ValidationMessageConstants {

    private ValidationMessageConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String MSG_NAME_REQUIRED = "Bootcamp name is required";
    public static final String MSG_NAME_MAX_LENGTH = "Bootcamp name must not exceed %d characters";
    public static final String MSG_DESCRIPTION_REQUIRED = "Bootcamp description is required";
    public static final String MSG_DESCRIPTION_MAX_LENGTH = "Bootcamp description must not exceed %d characters";
    public static final String MSG_LAUNCH_DATE_REQUIRED = "Bootcamp launch date is required";
    public static final String MSG_DURATION_IN_WEEKS_REQUIRED = "Bootcamp duration in weeks is required";
    public static final String MSG_DURATION_IN_WEEKS_MUST_BE_POSITIVE = "Bootcamp duration in weeks must be greater than zero";
    public static final String MSG_CAPABILITY_IDS_SIZE_RANGE = "Bootcamp must have between %d and %d capabilities";
    public static final String MSG_CAPABILITY_IDS_DUPLICATED = "Bootcamp capability ids must not contain duplicates";
}
