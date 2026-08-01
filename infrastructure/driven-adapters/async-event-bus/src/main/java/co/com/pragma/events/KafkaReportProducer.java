package co.com.pragma.events;

import co.com.pragma.events.dto.BootcampReportEventDto;
import co.com.pragma.model.bootcamp.gateways.ReportGateway;
import co.com.pragma.model.bootcamp.query.BootcampReportData;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import tools.jackson.databind.json.JsonMapper;

@Service
@Log4j2
public class KafkaReportProducer implements ReportGateway {

    private final KafkaSender<String, String> kafkaSender;
    private final String topic;
    private final JsonMapper jsonMapper = new JsonMapper();

    public KafkaReportProducer(KafkaSender<String, String> kafkaSender,
                                @Value("${adapters.kafka.producer.topic}") String topic) {
        this.kafkaSender = kafkaSender;
        this.topic = topic;
    }

    @Override
    public Mono<Void> registerBootcampReport(BootcampReportData reportData) {
        String payload = jsonMapper.writeValueAsString(toEventDto(reportData));
        String key = String.valueOf(reportData.bootcampId());
        SenderRecord<String, String, Long> record = SenderRecord.create(
                new ProducerRecord<>(topic, key, payload), reportData.bootcampId());

        return kafkaSender.send(Mono.just(record))
                .doOnNext(result -> log.info("Bootcamp report event published, bootcampId={}", reportData.bootcampId()))
                .then();
    }

    private BootcampReportEventDto toEventDto(BootcampReportData reportData) {
        return new BootcampReportEventDto(reportData.bootcampId(), reportData.name(), reportData.description(),
                reportData.launchDate(), reportData.durationInWeeks(), reportData.capabilityCount(),
                reportData.technologyCount(), reportData.enrolledPersonCount());
    }
}
