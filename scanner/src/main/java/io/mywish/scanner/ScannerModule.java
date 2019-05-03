package io.mywish.scanner;

import io.mywish.blockchain.WrapperModule;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan
@PropertySource("classpath:scanner.properties")
@Import(WrapperModule.class)
public class ScannerModule {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfig() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
