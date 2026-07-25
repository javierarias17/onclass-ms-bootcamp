package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("bootcamps")
public class BootcampEntity {
    @Id
    private Long id;
    private String name;
    private String description;
    private LocalDate launchDate;
    private Integer durationInWeeks;
    private String status;
    private Integer capabilityCount;
    @Version
    private Long version;
}
