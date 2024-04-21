package pl.smarthouse.model.diagnostic;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ModuleDetails {
  int reconnectCount;
  private String type;
  private String macAddress;
  private String serviceAddress;
  private String version;
  private LocalDateTime serviceUpdateTimestamp;
  private String moduleIpAddress;
  private String firmware;
  private LocalDateTime moduleUpdateTimestamp;
  private long uptimeInMinutes;

  public Long getServiceLastUpdateInSec() {
    return Duration.between(serviceUpdateTimestamp, LocalDateTime.now()).toSeconds();
  }

  public Long getModuleLastUpdateInSec() {
    return Duration.between(moduleUpdateTimestamp, LocalDateTime.now()).toSeconds();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ModuleDetails that = (ModuleDetails) o;
    return type.equals(that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type);
  }
}
