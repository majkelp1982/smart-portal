package pl.smarthouse.components;

import com.vaadin.flow.component.HtmlContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.Getter;
import pl.smarthouse.model.ColorPredicate;
import pl.smarthouse.model.ComponentColor;

@Getter
public abstract class PortalComponent {
  String valuePath;
  Object value;
  Object expectedValue;
  ComponentColor defaultColor;
  boolean colorEnabled;
  HtmlContainer component;
  List<ColorPredicate> colorPredicates = new ArrayList<>();

  public abstract void setValue(final Object value);

  public void addColorPredicates(
      final Predicate<PortalComponent> predicate, final ComponentColor color) {
    colorPredicates.add(new ColorPredicate(predicate, color));
  }

  public void setColorEnabled(final boolean colorEnabled) {
    this.colorEnabled = colorEnabled;
  }

  public void setDefaultColor(final ComponentColor defaultColor) {
    this.defaultColor = defaultColor;
  }

  public void setExpectedValue(final Object expectedValue) {
    this.expectedValue = expectedValue;
  }

  void setColor() {
    if (!colorEnabled) {
      component.getStyle().set("color", ComponentColor.NORMAL.value);
      return;
    }
    // please remember, only the last predicate will set color.
    if (!colorPredicates.isEmpty()) {
      if (Objects.nonNull(defaultColor)) {
        component.getStyle().set("color", defaultColor.value);
      }
    }
    colorPredicates.forEach(
        colorPredicate -> {
          if (colorPredicate.getPredicate().test(this)) {
            component.getStyle().set("color", colorPredicate.getColor().value);
          }
        });
  }
}
