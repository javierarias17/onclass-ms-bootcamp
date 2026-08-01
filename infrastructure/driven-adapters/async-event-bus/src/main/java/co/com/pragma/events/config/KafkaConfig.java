package co.com.pragma.events.config;

import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class KafkaConfig {

    @Bean
    public SenderOptions<String, String> kafkaSenderOptions(KafkaProperties kafkaProperties)
            throws UnknownHostException {
        // Set id based on hostname, customize here another properties
        kafkaProperties.setClientId(InetAddress.getLocalHost().getHostName());
        return SenderOptions.create(kafkaProperties.buildProducerProperties());
    }

    @Bean
    public KafkaSender<String, String> kafkaSender(SenderOptions<String, String> kafkaSenderOptions) {
        return KafkaSender.create(kafkaSenderOptions);
    }
}
