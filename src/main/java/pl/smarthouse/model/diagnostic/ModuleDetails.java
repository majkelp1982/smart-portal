package pl.smarthouse.model.diagnostic;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ModuleDetails {
  int reconnectCount;
  private String moduleType;
  private String macAddress;
  private String serviceAddress;
  private String version;
  private LocalDateTime serviceUpdateTimestamp;
  private String moduleIpAddress;
  private String firmware;
  private LocalDateTime moduleUpdateTimestamp;

  public Long getServiceLastUpdateInSec() {
    return Duration.between(serviceUpdateTimestamp, LocalDateTime.now()).toSeconds();
  }

  public Long getModuleLastUpdateInSec() {
    return Duration.between(moduleUpdateTimestamp, LocalDateTime.now()).toSeconds();
  }
}
