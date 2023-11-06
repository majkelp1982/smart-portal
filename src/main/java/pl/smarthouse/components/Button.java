package pl.smarthouse.components;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

@Setter
public class Button extends PortalComponent {
  private final HorizontalLayout layout = new HorizontalLayout();
  private final com.vaadin.flow.component.button.Button nameButton;

  public Button(final String name) {
    nameButton = new com.vaadin.flow.component.button.Button(name);
    create();
  }

  public Button(final String name, final boolean colorEnabled) {
    nameButton = new com.vaadin.flow.component.button.Button(name);
    this.colorEnabled = colorEnabled;
    create();
  }

  public com.vaadin.flow.component.button.Button getSource() {
    return nameButton;
  }

  public HorizontalLayout getLayout() {
    return layout;
  }

  private void create() {
    // Component to keep the same handling of colors like in Info object
    component = new com.vaadin.flow.component.html.Label();
    layout.add(nameButton);
  }

  @Override
  public void setValue(final Object value) {
    this.value = value;
    super.setColor();
    nameButton.getStyle().set("color", component.getStyle().get("color"));
  }
}
