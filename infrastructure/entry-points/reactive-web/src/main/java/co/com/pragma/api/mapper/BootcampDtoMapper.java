package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.BootcampInDto;
import co.com.pragma.api.dto.BootcampOutDto;
import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.command.BootcampCreateCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BootcampDtoMapper {

    BootcampCreateCommand toBootcampCreateCommand(BootcampInDto bootcampInDto);

    @Mapping(source = "bootcamp.id", target = "id")
    @Mapping(source = "bootcamp.name.value", target = "name")
    @Mapping(source = "bootcamp.description.value", target = "description")
    @Mapping(source = "bootcamp.launchDate.value", target = "launchDate")
    @Mapping(source = "bootcamp.durationInWeeks.value", target = "durationInWeeks")
    @Mapping(source = "capabilityIds", target = "capabilityIds")
    BootcampOutDto toBootcampOutDto(Bootcamp bootcamp, List<Long> capabilityIds);
}
