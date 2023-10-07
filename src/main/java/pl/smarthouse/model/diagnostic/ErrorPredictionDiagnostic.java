package pl.smarthouse.model.diagnostic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.*;

@Data
@NoArgsConstructor
public class ErrorPredictionDiagnostic {
  private String moduleName;
  private String message;
  private int priority;
  private LocalDateTime beginTimestamp;
  private LocalDateTime endTimestamp;

  public String getBeginTimeString() {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    if (beginTimestamp != null) {
      return beginTimestamp.format(formatter);
    } else {
      return null;
    }
  }

  public String getEndTimeString() {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    if (endTimestamp != null) {
      return endTimestamp.format(formatter);
    } else {
      return null;
    }
  }
}
