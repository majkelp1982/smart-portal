package pl.smarthouse.properties;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ModuleManagerProperties {
  private final String URL = "http://192.168.0.200:9999";
}
