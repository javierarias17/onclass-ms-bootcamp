package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.BootcampInDto;
import co.com.pragma.api.dto.BootcampListItemOutDto;
import co.com.pragma.api.dto.BootcampOutDto;
import co.com.pragma.api.dto.BootcampPageOutDto;
import co.com.pragma.api.dto.BootcampScheduleOutDto;
import co.com.pragma.api.dto.BootcampSchedulesOutDto;
import co.com.pragma.api.dto.CapabilitySummaryOutDto;
import co.com.pragma.api.dto.TechnologySummaryOutDto;
import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.command.BootcampCreateCommand;
import co.com.pragma.model.bootcamp.query.BootcampListItem;
import co.com.pragma.model.bootcamp.query.BootcampPage;
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

    default BootcampPageOutDto toBootcampPageOutDto(BootcampPage page) {
        List<BootcampListItemOutDto> content = page.content().stream()
                .map(this::toBootcampListItemOutDto)
                .toList();
        return new BootcampPageOutDto(content, page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    default BootcampListItemOutDto toBootcampListItemOutDto(BootcampListItem item) {
        List<CapabilitySummaryOutDto> capabilities = item.capabilities().stream()
                .map(capability -> new CapabilitySummaryOutDto(capability.id(), capability.name()))
                .toList();
        List<TechnologySummaryOutDto> technologies = item.technologies().stream()
                .map(technology -> new TechnologySummaryOutDto(technology.id(), technology.name()))
                .toList();
        return new BootcampListItemOutDto(item.bootcamp().getId(), item.bootcamp().getName().value(),
                item.bootcamp().getDescription().value(), item.bootcamp().getLaunchDate().value(),
                item.bootcamp().getDurationInWeeks().value(), capabilities, technologies);
    }

    default BootcampSchedulesOutDto toBootcampSchedulesOutDto(List<Bootcamp> bootcamps) {
        List<BootcampScheduleOutDto> schedules = bootcamps.stream()
                .map(bootcamp -> new BootcampScheduleOutDto(bootcamp.getId(), bootcamp.getLaunchDate().value(),
                        bootcamp.getDurationInWeeks().value()))
                .toList();
        return new BootcampSchedulesOutDto(schedules);
    }
}
