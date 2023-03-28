package pl.smarthouse.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class GuiComfortServiceProperties {
  public static final String COMFORT_TYPE_PREFIX = "COMFORT";
  private boolean isSynced;
}
