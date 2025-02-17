package pl.smarthouse.views.utils;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import pl.smarthouse.components.Button;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.NativeLabel;
import pl.smarthouse.components.PortalComponent;
import pl.smarthouse.model.ComponentColor;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRangeMode;
import pl.smarthouse.sharedobjects.dto.core.enums.SpaDeviceState;
import pl.smarthouse.sharedobjects.dto.core.enums.State;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.Mode;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.ThrottleState;
import pl.smarthouse.sharedobjects.dto.weather.SunState;
import pl.smarthouse.sharedobjects.enums.Operation;

@UtilityClass
public class ColorPredicates {

  public void assignOnOffState(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OFF);
    info.addColorPredicates(
        component -> State.ON.toString().equals(component.getValue().toString()),
        ComponentColor.ON);
  }

  public void assignOnOffState(final Button button) {
    button.setColorEnabled(true);
    button.setDefaultColor(ComponentColor.OFF);
    button.addColorPredicates(
        component -> State.ON.toString().equals(component.getValue().toString()),
        ComponentColor.ON);
  }

  public void assignEnableState(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OK);
    info.addColorPredicates(
        component -> State.OFF.toString().equals(component.getValue().toString()),
        ComponentColor.ALARM);
  }

  public void assignTrueFalseState(final Button button) {
    button.setColorEnabled(true);
    button.setDefaultColor(ComponentColor.OFF);
    button.addColorPredicates(component -> (boolean) component.getValue(), ComponentColor.ON);
  }

  public void assignNotZeroState(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OFF);
    info.addColorPredicates(
        component -> Integer.valueOf(component.getValue().toString()) > 0, ComponentColor.ON);
  }

  public void assignToValue(final Info info, final double warning, final double alarm) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OK);
    info.addColorPredicates(
        component -> Double.valueOf(component.getValue().toString()) >= warning,
        ComponentColor.WARNING);
    info.addColorPredicates(
        component -> Double.valueOf(component.getValue().toString()) >= alarm,
        ComponentColor.ALARM);
  }

  public void assignToSun(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OK);
    info.addColorPredicates(
        component -> SunState.SET.equals(component.getValue()), ComponentColor.SET);
    info.addColorPredicates(
        component -> SunState.RISE.equals(component.getValue()), ComponentColor.RISE);
  }

  public void assignToCurrentOperation(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OFF);
    info.addColorPredicates(
        component -> !Operation.STANDBY.equals(component.getValue()), ComponentColor.ON);
  }

  public void assignToMode(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OFF);
    info.addColorPredicates(
        component -> Mode.ERROR.equals(component.getValue()), ComponentColor.ALARM);
    final List onModes = List.of(Mode.STANDBY, Mode.HEATING);
    info.addColorPredicates(component -> onModes.contains(component.getValue()), ComponentColor.ON);
    info.addColorPredicates(
        component -> Mode.COOLING.equals(component.getValue()), ComponentColor.WARNING);
  }

  public void assignToSpaDeviceRelayState(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OFF);
    info.addColorPredicates(
        component -> SpaDeviceState.OFF.equals(component.getValue()), ComponentColor.OFF);
    info.addColorPredicates(
        component -> SpaDeviceState.ON.equals(component.getValue()), ComponentColor.ON);
    info.addColorPredicates(
        component -> SpaDeviceState.MIN_TEMP_REQ.equals(component.getValue()),
        ComponentColor.WARNING);
  }

  public void assignToTimeRangeMode(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OK);
    info.addColorPredicates(
        component -> !TimeRangeMode.AUTO.equals(component.getValue()), ComponentColor.WARNING);
  }

  public void assignToThrottleState(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OFF);
    info.addColorPredicates(
        component -> ThrottleState.OPEN.equals(component.getValue()), ComponentColor.ON);
  }

  public void assignToRequiredPower(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.OFF);
    info.addColorPredicates(component -> (int) component.getValue() > 0, ComponentColor.ON);
  }

  public void assignToHumidity(final Info info) {
    info.setColorEnabled(true);
    info.addColorPredicates(
        component -> ((int) component.getValue() < 35 || (int) component.getValue() > 70),
        ComponentColor.ALARM);
    info.addColorPredicates(
        component -> ((int) component.getValue() >= 35 && (int) component.getValue() <= 70),
        ComponentColor.WARNING);
    info.addColorPredicates(
        component -> ((int) component.getValue() >= 40 && (int) component.getValue() <= 60),
        ComponentColor.OK);
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

  public void assignToUpdateTimestamp(final Info info) {
    assignToUpdateTimestamp(info, 2);
  }

  public void assignToUpdateTimestamp(final Info info, final int timeoutInMinutes) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.NORMAL);
    info.addColorPredicates(
        component -> {
          final LocalTime updateTimestamp = LocalTime.parse(component.getValue().toString());
          return (Objects.isNull(updateTimestamp)
              || LocalTime.now().isAfter(updateTimestamp.plusMinutes(timeoutInMinutes)));
        },
        ComponentColor.ALARM);
  }

  public void assignToError(final Info info) {
    info.setColorEnabled(true);
    info.setDefaultColor(ComponentColor.NORMAL);
    info.addColorPredicates(component -> (boolean) component.getValue(), ComponentColor.ALARM);
  }

  public void assignToError(final NativeLabel label) {
    label.setColorEnabled(true);
    label.setDefaultColor(ComponentColor.NORMAL);
    label.addColorPredicates(component -> (boolean) component.getValue(), ComponentColor.ALARM);
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
