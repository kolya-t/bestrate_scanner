package io.lastwill.eventscan;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lastwill.eventscan.events.EventModule;
import io.lastwill.eventscan.services.MQConsumer;
import io.mywish.scanner.ScannerModule;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.net.URI;

@SpringBootApplication
@Import({ScannerModule.class, EventModule.class, })
@EntityScan(basePackageClasses = {Application.class, Jsr310JpaConverters.class})
@EnableRabbit
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .addCommandLineProperties(true)
                .web(false)
                .sources(Application.class)
                .main(Application.class)
                .run(args);

    }

    @Bean(destroyMethod = "close")
    public CloseableHttpClient closeableHttpClient(
            @Value("${io.lastwill.eventscan.backend.get-connection-timeout}") int getConnectionTimeout,
            @Value("${io.lastwill.eventscan.backend.connection-timeout}") int connectionTimeout,
            @Value("${io.lastwill.eventscan.backend.socket-timeout}") int socketTimeout) {

        return HttpClientBuilder
                .create()
                .setMaxConnPerRoute(50)
                .setMaxConnTotal(200)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(connectionTimeout)
                                .setSocketTimeout(socketTimeout)
                                .setConnectionRequestTimeout(getConnectionTimeout)
                                .setCookieSpec(CookieSpecs.STANDARD)
                                .build()
                )
                .setConnectionManagerShared(true)
                .build();
    }

    @Bean
    public ConnectionFactory connectionFactory(@Value("${io.lastwill.eventscan.mq.url}") URI uri) {
        return new CachingConnectionFactory(uri);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

//    @Bean(MQConsumer.SUBSCRIBE_QUEUE_NAME)
//    public Queue subscribeQueue(@Value("${io.lastwill.eventscan.mq.subscribe-queue.name}") String queueName) {
//        return new Queue(queueName, true);
//    }
//
//    @Bean(MQConsumer.UNSUBSCRIBE_QUEUE_NAME)
//    public Queue unsubscribeQueue(@Value("${io.lastwill.eventscan.mq.unsubscribe-queue.name}") String queueName) {
//        return new Queue(queueName, true);
//    }

    @Bean
    public DirectExchange directExchange(@Value("${io.lastwill.eventscan.mq.exchange.name}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }


//    @Bean
//    public OkHttpClient okHttpClient(
//            @Value("${io.lastwill.eventscan.backend.socket-timeout}") long socketTimeout,
//            @Value("${io.lastwill.eventscan.backend.connection-timeout}") long connectionTimeout
//    ) {
//        return new OkHttpClient.Builder()
//                .writeTimeout(socketTimeout, TimeUnit.MILLISECONDS)
//                .readTimeout(socketTimeout, TimeUnit.MILLISECONDS)
//                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
//                .build();
//    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                // TODO: remove it!
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }
}
