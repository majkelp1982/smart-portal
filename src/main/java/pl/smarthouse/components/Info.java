package pl.smarthouse.components;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;

public class Info extends PortalComponent {
  private final HorizontalLayout layout = new HorizontalLayout();
  private final String name;
  private final Label valueLabel = new Label();
  private String unit;
  private boolean colorEnabled;

  public Info(final String name, final String unit) {
    this.name = name;
    this.unit = unit;
    create();
  }

  public Info(final String name) {
    this.name = name;
    create();
  }

  public Info(final String name, final String unit, final boolean colorEnabled) {
    this.name = name;
    this.unit = unit;
    this.colorEnabled = colorEnabled;
    create();
  }

  public HorizontalLayout getLayout() {
    return layout;
  }

  private void create() {
    layout.add(new Label(name), valueLabel);
  }

  @Override
  public void setValue(final Number value) {
    final StringBuilder str = new StringBuilder();
    str.append(value);
    if (Objects.nonNull(unit)) {
      str.append("[");
      str.append(unit);
      str.append("]");
    }
    valueLabel.setText(str.toString());
  }
}
