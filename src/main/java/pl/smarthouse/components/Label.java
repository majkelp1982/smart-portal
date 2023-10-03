package pl.smarthouse.components;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

@Setter
public class Label extends PortalComponent {
  private final HorizontalLayout layout = new HorizontalLayout();
  private final com.vaadin.flow.component.html.Label nameLabel;

  public Label(final String name) {
    nameLabel = new com.vaadin.flow.component.html.Label(name);
    create();
  }

  public Label(final String name, final boolean colorEnabled) {
    nameLabel = new com.vaadin.flow.component.html.Label(name);
    this.colorEnabled = colorEnabled;
    create();
  }

  public HorizontalLayout getLayout() {
    return layout;
  }

  private void create() {
    // Component to keep the same handling of colors like in Info object
    component = new com.vaadin.flow.component.html.Label();
    layout.add(nameLabel);
  }

  @Override
  public void setValue(final Object value) {
    this.value = value;
    super.setColor();
    nameLabel.getStyle().set("color", component.getStyle().get("color"));
  }
}
