package co.com.pragma.model.bootcamp;

import co.com.pragma.model.bootcamp.valueobject.BootcampDescription;
import co.com.pragma.model.bootcamp.valueobject.BootcampDurationInWeeks;
import co.com.pragma.model.bootcamp.valueobject.BootcampLaunchDate;
import co.com.pragma.model.bootcamp.valueobject.BootcampName;
import co.com.pragma.model.exceptions.FieldsValidationException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Bootcamp {

    private final Long id;
    private final BootcampName name;
    private final BootcampDescription description;
    private final BootcampLaunchDate launchDate;
    private final BootcampDurationInWeeks durationInWeeks;
    private final BootcampStatusEnum status;
    private final Integer capabilityCount;
    private final Long version;

    private Bootcamp(Builder builder) {
        this.id = builder.id;
        this.name = new BootcampName(builder.name);
        this.description = new BootcampDescription(builder.description);
        this.launchDate = new BootcampLaunchDate(builder.launchDate);
        this.durationInWeeks = new BootcampDurationInWeeks(builder.durationInWeeks);
        this.status = builder.status;
        this.capabilityCount = builder.capabilityCount;
        this.version = builder.version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public BootcampName getName() {
        return name;
    }

    public BootcampDescription getDescription() {
        return description;
    }

    public BootcampLaunchDate getLaunchDate() {
        return launchDate;
    }

    public BootcampDurationInWeeks getDurationInWeeks() {
        return durationInWeeks;
    }

    public BootcampStatusEnum getStatus() {
        return status;
    }

    public Integer getCapabilityCount() {
        return capabilityCount;
    }

    public Long getVersion() {
        return version;
    }

    public static class Builder {

        private Long id;
        private String name;
        private String description;
        private LocalDate launchDate;
        private Integer durationInWeeks;
        private BootcampStatusEnum status;
        private Integer capabilityCount;
        private Long version;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder launchDate(LocalDate launchDate) {
            this.launchDate = launchDate;
            return this;
        }

        public Builder durationInWeeks(Integer durationInWeeks) {
            this.durationInWeeks = durationInWeeks;
            return this;
        }

        public Builder status(BootcampStatusEnum status) {
            this.status = status;
            return this;
        }

        public Builder capabilityCount(Integer capabilityCount) {
            this.capabilityCount = capabilityCount;
            return this;
        }

        public Bootcamp build() {
            Map<String, String> errors = new LinkedHashMap<>();

            BootcampName.validate(name, errors);
            BootcampDescription.validate(description, errors);
            BootcampLaunchDate.validate(launchDate, errors);
            BootcampDurationInWeeks.validate(durationInWeeks, errors);

            if (!errors.isEmpty())
                throw new FieldsValidationException(errors);

            return new Bootcamp(this);
        }
    }
}
