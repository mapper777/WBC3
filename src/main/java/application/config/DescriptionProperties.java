package application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@PropertySource(value = "classpath:description.properties", encoding = "UTF-8")
@ConfigurationProperties(prefix = "description")
@Getter
@Setter
public class DescriptionProperties {
    private String langLabel;
    private String scanLabel;
    private String parsLabel;
    private Map<String, String> langMap;
    private Map<String, String> scanMap;
    private Map<String, String> parsMap;
}

