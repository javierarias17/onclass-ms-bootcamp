package co.com.pragma.model.common;

import co.com.pragma.model.bootcamp.query.BootcampSortFieldEnum;
import co.com.pragma.model.bootcamp.query.SortDirectionEnum;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    public static final String MSG_ID_REQUIRED = "Bootcamp id is required";
    public static final String MSG_ID_MUST_BE_NUMERIC = "Bootcamp id must be numeric";
    public static final String MSG_PAGE_MUST_BE_NUMERIC = "Page must be numeric";
    public static final String MSG_PAGE_OUT_OF_RANGE = "Page must be zero or greater";
    public static final String MSG_SIZE_MUST_BE_NUMERIC = "Size must be numeric";
    public static final String MSG_SIZE_OUT_OF_RANGE = "Size must be between %d and %d";
    public static final String MSG_SORT_BY_INVALID = "Sort field must be one of: " + allowedNames(BootcampSortFieldEnum.values());
    public static final String MSG_SORT_DIRECTION_INVALID = "Sort direction must be one of: " + allowedNames(SortDirectionEnum.values());

    private static String allowedNames(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.joining(", "));
    }
}
