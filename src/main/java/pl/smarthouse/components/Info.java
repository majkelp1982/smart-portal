package pl.smarthouse.components;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;
import lombok.Setter;

@Setter
public class Info extends PortalComponent {
  private final HorizontalLayout layout = new HorizontalLayout();
  private final String name;
  private final String unit;

  public Info(final String name, final String unit) {
    this.name = name;
    this.unit = unit;
    create();
  }

  public Info(final String name) {
    this.name = name;
    unit = null;
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
    component = new Label();
    layout.add(new Label(name), component);
  }

  @Override
  public void setValue(final Object value) {
    this.value = value;
    super.setColor();
    final StringBuilder str = new StringBuilder();
    str.append(this.value);
    if (Objects.nonNull(unit)) {
      str.append("[");
      str.append(unit);
      str.append("]");
    }
    component.setText(str.toString());
  }
}
