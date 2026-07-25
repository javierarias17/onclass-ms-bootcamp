package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.BootcampStatusEnum;
import co.com.pragma.r2dbc.entity.BootcampEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BootcampEntityMapper {

    @Mapping(source = "name.value", target = "name")
    @Mapping(source = "description.value", target = "description")
    @Mapping(source = "launchDate.value", target = "launchDate")
    @Mapping(source = "durationInWeeks.value", target = "durationInWeeks")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "capabilityCount", target = "capabilityCount")
    BootcampEntity toEntity(Bootcamp bootcamp);

    default Bootcamp toDomain(BootcampEntity entity) {
        return entity == null ? null
                : Bootcamp.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .description(entity.getDescription())
                        .launchDate(entity.getLaunchDate())
                        .durationInWeeks(entity.getDurationInWeeks())
                        .status(BootcampStatusEnum.valueOf(entity.getStatus()))
                        .capabilityCount(entity.getCapabilityCount())
                        .version(entity.getVersion())
                        .build();
    }
}
