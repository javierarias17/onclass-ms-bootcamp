package co.com.pragma.model.bootcamp.gateways;

import co.com.pragma.model.bootcamp.query.BootcampReportData;
import reactor.core.publisher.Mono;

public interface ReportGateway {

    Mono<Void> registerBootcampReport(BootcampReportData reportData);
}
