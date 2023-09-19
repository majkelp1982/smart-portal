package pl.smarthouse.views.comfort;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.PortalComponent;
import pl.smarthouse.model.ComponentColor;
import pl.smarthouse.sharedobjects.enums.Operation;

@UtilityClass
public class ColorPredicates {

  public void assignToCurrentOperation(final Info info) {
    info.setColorEnabled(true);
    info.addColorPredicates(
        component -> !Operation.STANDBY.equals(component.getValue()), ComponentColor.ON);
    info.addColorPredicates(
        operation -> Operation.STANDBY.equals(operation.getValue()), ComponentColor.OFF);
  }

  public void assignToRequiredPower(final Info info) {
    info.setColorEnabled(true);
    info.addColorPredicates(component -> (int) component.getValue() > 0, ComponentColor.ON);
    info.addColorPredicates(component -> (int) component.getValue() == 0, ComponentColor.OFF);
  }

  public void assignToTemperature(
      final PortalComponent portalComponent,
      final Double cold,
      final Double tolerance,
      final Double hot) {
    if (tolerance <= 0) {
      throw new IllegalArgumentException("tolerance must be greater than 0");
    }
    if (cold > 0 && tolerance < Math.abs(cold)) {
      throw new IllegalArgumentException("cold must be <0 and abs greater than tolerance");
    }
    if (hot < tolerance) {
      throw new IllegalArgumentException("hot must be greater than tolerance");
    }
    portalComponent.setColorEnabled(true);
    // Color UNDER
    portalComponent.addColorPredicates(
        component -> {
          final double delta = delta(component.getValue(), component.getExpectedValue());
          return delta <= -tolerance;
        },
        ComponentColor.UNDER);

    // Color COLD
    portalComponent.addColorPredicates(
        component -> {
          final double delta = delta(component.getValue(), component.getExpectedValue());
          return delta <= cold;
        },
        ComponentColor.COLD);

    // Color GOOD
    portalComponent.addColorPredicates(
        component -> {
          final double absDelta =
              Math.abs(delta(component.getValue(), component.getExpectedValue()));
          return absDelta < tolerance;
        },
        ComponentColor.GOOD);

    // Color OVER
    portalComponent.addColorPredicates(
        component -> {
          final double delta = delta(component.getValue(), component.getExpectedValue());
          return delta >= tolerance;
        },
        ComponentColor.OVER);

    // Color HOT
    portalComponent.addColorPredicates(
        component -> {
          final double delta = delta(component.getValue(), component.getExpectedValue());
          return delta >= hot;
        },
        ComponentColor.HOT);
  }

  private double delta(final Object value1, final Object value2) {
    if (Objects.isNull(value1) || Objects.isNull(value2)) {
      return 0;
    }
    try {
      final Double doubleValue1 = Double.parseDouble(value1.toString());
      final Double doubleValue2 = Double.parseDouble(value2.toString());
      return (doubleValue1 - doubleValue2);
    } catch (final Exception e) {
      return 0;
    }
  }
}
