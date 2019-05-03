package io.lastwill.eventscan;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lastwill.eventscan.events.EventModule;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import io.mywish.scanner.ScannerModule;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@Import({ScannerModule.class, EventModule.class,})
@EntityScan(basePackageClasses = {Application.class, Jsr310JpaConverters.class})
@EnableRabbit
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .addCommandLineProperties(true)
                .sources(Application.class)
                .main(Application.class)
                .run(args);
    }

    @Autowired
    private SubscriptionRepository subscriptionRepository;

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
    public ConnectionFactory connectionFactory(
            @Value("${eventscan.mq.host}") String host,
            @Value("${eventscan.mq.port}") int port,
            @Value("${eventscan.mq.username}") String username,
            @Value("${eventscan.mq.password}") String password,
            @Value("${eventscan.mq.vhost}") String vhost
    ) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(vhost);
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enableDefaultTyping()
                .registerModule(new JavaTimeModule());
    }

    @Bean
    public MessageConverter jsonConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            @Value("${io.lastwill.eventscan.backend-mq.ttl-ms}") String ttl
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        MessagePropertiesBuilder.newInstance().setExpiration(ttl).build();

        return rabbitTemplate;
    }

    @Bean
    public Queue subscribeQueue(@Value("${eventscan.mq.subscribe-queue.name}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue unsubscribeQueue(@Value("${eventscan.mq.unsubscribe-queue.name}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange directExchange(@Value("${eventscan.mq.exchange.name}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }
}
