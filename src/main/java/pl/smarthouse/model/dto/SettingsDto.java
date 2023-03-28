package pl.smarthouse.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SettingsDto {
  private String moduleType;
  private String macAddress;
  private String version;
  private String firmware;
}
