package pl.smarthouse.components;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;
import lombok.Setter;

@Setter
public class NativeLabel extends PortalComponent {
  @Getter private final HorizontalLayout layout = new HorizontalLayout();
  private final com.vaadin.flow.component.html.NativeLabel nameLabel;

  public NativeLabel(final String name) {
    nameLabel = new com.vaadin.flow.component.html.NativeLabel(name);
    create();
  }

  public NativeLabel(final String name, final boolean colorEnabled) {
    nameLabel = new com.vaadin.flow.component.html.NativeLabel(name);
    this.colorEnabled = colorEnabled;
    create();
  }

  private void create() {
    // Component to keep the same handling of colors like in Info object
    component = new com.vaadin.flow.component.html.NativeLabel();
    layout.add(nameLabel);
  }

  @Override
  public void setValue(final Object value) {
    this.value = value;
    super.setColor();
    nameLabel.getStyle().set("color", component.getStyle().get("color"));
  }
}
