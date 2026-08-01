package co.com.pragma.consumer.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
public class RestConsumerConfig {

    public static final String CAPABILITY_WEB_CLIENT = "capabilityWebClient";
    public static final String PERSON_WEB_CLIENT = "personWebClient";

    private final String capabilityUrl;
    private final String personUrl;
    private final int timeout;

    public RestConsumerConfig(@Value("${adapter.restconsumer.capabilityUrl}") String capabilityUrl,
                              @Value("${adapter.restconsumer.personUrl}") String personUrl,
                              @Value("${adapter.restconsumer.timeout}") int timeout) {
        this.capabilityUrl = capabilityUrl;
        this.personUrl = personUrl;
        this.timeout = timeout;
    }

    @Bean(CAPABILITY_WEB_CLIENT)
    public WebClient getWebClient() {
        return WebClient.builder()
            .baseUrl(capabilityUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(getClientHttpConnector())
            .build();
    }

    @Bean(PERSON_WEB_CLIENT)
    public WebClient getPersonWebClient() {
        return WebClient.builder()
            .baseUrl(personUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(getClientHttpConnector())
            .build();
    }

    private ClientHttpConnector getClientHttpConnector() {
        /*
        IF YO REQUIRE APPEND SSL CERTIFICATE SELF SIGNED: this should be in the default cacerts trustore
        */
        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }

}
