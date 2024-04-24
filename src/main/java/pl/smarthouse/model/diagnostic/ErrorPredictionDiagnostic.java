package pl.smarthouse.model.diagnostic;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.*;

@Data
@NoArgsConstructor
public class ErrorPredictionDiagnostic {
  private String hashCode;
  private String type;
  private String message;
  private int priority;
  private LocalDateTime beginTimestamp;
  private LocalDateTime endTimestamp;
  private long duration;
  // if grouped
  private int errorCount;
  private long totalTimeInMinutes;

  public String getBeginTimeString() {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    if (beginTimestamp != null) {
      return beginTimestamp.format(formatter);
    } else {
      return null;
    }
  }

  public ErrorPredictionDiagnostic get() {
    return this;
  }

  public String getEndTimeString() {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    if (endTimestamp != null) {
      return endTimestamp.format(formatter);
    } else {
      return null;
    }
  }

  public Long getDuration() {
    return Duration.between(
            beginTimestamp, (endTimestamp != null) ? endTimestamp : LocalDateTime.now())
        .toSeconds();
  }
}
