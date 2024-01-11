package pl.smarthouse.views.comfort.subview;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import java.util.HashSet;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import pl.smarthouse.components.params.PercentageField;
import pl.smarthouse.components.params.TemperatureField;
import pl.smarthouse.components.params.TimeRangesGrid;
import pl.smarthouse.sharedobjects.dto.comfort.core.ForcedAirControl;
import pl.smarthouse.sharedobjects.dto.comfort.core.HeatingControl;
import pl.smarthouse.sharedobjects.dto.comfort.core.TemperatureControl;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.FunctionType;

@UtilityClass
public class TemperatureControlView {
  public void addForm(
      final Accordion accordion,
      final FunctionType functionType,
      final TemperatureControl temperatureControl) {
    validateAirExchanger(temperatureControl);
    final VerticalLayout layout = new VerticalLayout();

    final NumberField requiredTemperatureField =
        new TemperatureField("required temperature", 20, 27);
    requiredTemperatureField.setValue(temperatureControl.getRequiredTemperature());
    requiredTemperatureField.addValueChangeListener(
        event -> temperatureControl.setRequiredTemperature(requiredTemperatureField.getValue()));

    final DatePicker disableTimeRangesUntilDatePicker = new DatePicker();
    disableTimeRangesUntilDatePicker.setLabel("disable time ranges until");
    disableTimeRangesUntilDatePicker.setValue(temperatureControl.getDisableTimeRangesUntilDate());
    disableTimeRangesUntilDatePicker.addValueChangeListener(
        event -> temperatureControl.setDisableTimeRangesUntilDate(event.getValue()));

    final Accordion temperatureControlAccordion = new Accordion();
    temperatureControlAccordion.add(
        "Heating control", heatingControlView(temperatureControl.getHeatingControl()));
    if (functionType == null || FunctionType.AIR_SUPPLY.equals(functionType)) {
      temperatureControlAccordion.add(
          "Forced air control", forcedAirControl(temperatureControl.getForcedAirControl()));
    }

    temperatureControlAccordion.close();

    layout.add(
        requiredTemperatureField, disableTimeRangesUntilDatePicker, temperatureControlAccordion);
    accordion.add("Temperature control", layout);
  }

  private VerticalLayout heatingControlView(final HeatingControl heatingControl) {
    final VerticalLayout layout = new VerticalLayout();

    final Checkbox enabledCheckBox = new Checkbox("enabled");
    enabledCheckBox.setValue(heatingControl.isHeatingEnabled());
    enabledCheckBox.addValueChangeListener(
        event -> heatingControl.setHeatingEnabled(enabledCheckBox.getValue()));

    final NumberField lowToleranceField = new TemperatureField("low tolerance", 0.1, 2.0);
    lowToleranceField.setValue(heatingControl.getLowTolerance());
    lowToleranceField.addValueChangeListener(
        event -> heatingControl.setLowTolerance(lowToleranceField.getValue()));

    final NumberField overheatingOn2TariffField =
        new TemperatureField("overheat on tariff II", 0.1, 1.0);
    overheatingOn2TariffField.setValue(heatingControl.getOverheatingOn2Tariff());
    overheatingOn2TariffField.addValueChangeListener(
        event -> heatingControl.setOverheatingOn2Tariff(overheatingOn2TariffField.getValue()));

    final TimeRangesGrid workDayGrid =
        new TimeRangesGrid("Workday time ranges", heatingControl.getWorkdayTimeRanges());

    final TimeRangesGrid weekendGrid =
        new TimeRangesGrid("Weekend time ranges", heatingControl.getWeekendTimeRanges());

    layout.add(
        enabledCheckBox, lowToleranceField, overheatingOn2TariffField, workDayGrid, weekendGrid);

    return layout;
  }

  private VerticalLayout forcedAirControl(final ForcedAirControl forcedAirControl) {
    final VerticalLayout layout = new VerticalLayout();

    // Air condition
    final Checkbox airConditionEnabled = new Checkbox("air condition enabled");
    airConditionEnabled.setValue(forcedAirControl.isAirConditionEnabled());
    airConditionEnabled.addValueChangeListener(
        event -> forcedAirControl.setAirConditionEnabled(airConditionEnabled.getValue()));

    final NumberField airConditionToleranceField =
        new TemperatureField(
            "air condition tolerance",
            forcedAirControl.getForcedAirTolerance(),
            forcedAirControl.getForcedAirTolerance() + 1.0);
    airConditionToleranceField.setValue(forcedAirControl.getAirConditionTolerance());
    airConditionToleranceField.addValueChangeListener(
        event -> forcedAirControl.setAirConditionTolerance(airConditionToleranceField.getValue()));

    final IntegerField airConditionRequiredPowerField =
        new PercentageField("air condition required power");
    airConditionRequiredPowerField.setValue(forcedAirControl.getAirConditionRequiredPower());
    airConditionRequiredPowerField.addValueChangeListener(
        event ->
            forcedAirControl.setAirConditionRequiredPower(
                airConditionRequiredPowerField.getValue()));

    // Forced air
    final Checkbox forcedAirEnabled = new Checkbox("forced air enabled");
    forcedAirEnabled.setValue(forcedAirControl.isForcedAirEnabled());
    forcedAirEnabled.addValueChangeListener(
        event -> forcedAirControl.setForcedAirEnabled(forcedAirEnabled.getValue()));

    final NumberField forcedAirToleranceField =
        new TemperatureField("forced air tolerance", 0.1, 1.0);
    forcedAirToleranceField.setValue(forcedAirControl.getForcedAirTolerance());
    forcedAirToleranceField.addValueChangeListener(
        event -> {
          forcedAirControl.setForcedAirTolerance(forcedAirToleranceField.getValue());
          airConditionToleranceField.setMin(forcedAirToleranceField.getValue() + 0.1);
          airConditionToleranceField.setMax(forcedAirToleranceField.getValue() + 2.0);
          airConditionToleranceField.setValue(airConditionToleranceField.getMin());
        });

    final IntegerField forcedAirRequiredPowerField =
        new PercentageField("forced air required power");
    forcedAirRequiredPowerField.setValue(forcedAirControl.getForcedAirRequiredPower());
    forcedAirRequiredPowerField.addValueChangeListener(
        event ->
            forcedAirControl.setForcedAirRequiredPower(forcedAirRequiredPowerField.getValue()));

    final TimeRangesGrid workDayGrid =
        new TimeRangesGrid("Workday time ranges", forcedAirControl.getWorkdayTimeRanges());

    final TimeRangesGrid weekendGrid =
        new TimeRangesGrid("Weekend time ranges", forcedAirControl.getWeekendTimeRanges());

    layout.add(
        forcedAirEnabled,
        forcedAirToleranceField,
        forcedAirRequiredPowerField,
        airConditionEnabled,
        airConditionToleranceField,
        airConditionRequiredPowerField,
        workDayGrid,
        weekendGrid);

    return layout;
  }

  private void validateAirExchanger(final TemperatureControl temperatureControl) {
    if (Objects.isNull(temperatureControl.getHeatingControl().getWorkdayTimeRanges())) {
      temperatureControl.getHeatingControl().setWorkdayTimeRanges(new HashSet<>());
    }
    if (Objects.isNull(temperatureControl.getHeatingControl().getWeekendTimeRanges())) {
      temperatureControl.getHeatingControl().setWeekendTimeRanges(new HashSet<>());
    }
    if (Objects.isNull(temperatureControl.getForcedAirControl().getWorkdayTimeRanges())) {
      temperatureControl.getForcedAirControl().setWorkdayTimeRanges(new HashSet<>());
    }
    if (Objects.isNull(temperatureControl.getForcedAirControl().getWeekendTimeRanges())) {
      temperatureControl.getForcedAirControl().setWeekendTimeRanges(new HashSet<>());
    }
  }
}
