package pl.smarthouse.model;

public enum ComponentColor {
  // State
  ALARM("orangered"),
  WARNING("yellow"),
  OK("lime"),

  // Value sync
  NORMAL("white"),
  NEW("orange"),

  // Sun
  SET("black"),
  RISE("yellow"),

  // Switch
  OFF("grey"),
  ON("lime"),

  // Temperature
  COLD("aqua"),
  UNDER("yellowgreen"),
  GOOD("lime"),
  OVER("yellow"),
  HOT("orangered");

  public final String value;

  ComponentColor(final String value) {
    this.value = value;
  }
}
