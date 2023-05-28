package pl.smarthouse.components;

import lombok.Getter;

@Getter
public abstract class PortalComponent {
  String valuePath;

  public abstract void setValue(final Number value);
}
