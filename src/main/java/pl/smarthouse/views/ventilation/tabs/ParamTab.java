package pl.smarthouse.views.ventilation.tabs;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import pl.smarthouse.components.params.PercentageField;
import pl.smarthouse.components.params.TimeRangesGrid;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;

public class ParamTab {

  public VerticalLayout get(final VentModuleParamsDto ventModuleParamsDto) {
    final VerticalLayout layout = new VerticalLayout();

    final Checkbox fireplaceAirOverpressureCheckBox = new Checkbox("fireplace air overpressure");
    fireplaceAirOverpressureCheckBox.setValue(
        ventModuleParamsDto.isFireplaceAirOverpressureEnabled());
    fireplaceAirOverpressureCheckBox.addValueChangeListener(
        event ->
            ventModuleParamsDto.setFireplaceAirOverpressureEnabled(
                fireplaceAirOverpressureCheckBox.getValue()));

    final IntegerField fireplaceAirOverpressureLevelField =
        new PercentageField("air overpressure level");
    fireplaceAirOverpressureLevelField.setValue(
        ventModuleParamsDto.getFireplaceAirOverpressureLevel());
    fireplaceAirOverpressureLevelField.addValueChangeListener(
        event ->
            ventModuleParamsDto.setFireplaceAirOverpressureLevel(
                fireplaceAirOverpressureLevelField.getValue()));

    final Checkbox humidityAlertCheckBox = new Checkbox("humidity alert");
    humidityAlertCheckBox.setValue(ventModuleParamsDto.isHumidityAlertEnabled());
    humidityAlertCheckBox.addValueChangeListener(
        event -> ventModuleParamsDto.setHumidityAlertEnabled(humidityAlertCheckBox.getValue()));

    final Checkbox airExchangeCheckBox = new Checkbox("air exchange");
    airExchangeCheckBox.setValue(ventModuleParamsDto.isAirExchangeEnabled());
    airExchangeCheckBox.addValueChangeListener(
        event -> ventModuleParamsDto.setAirExchangeEnabled(airExchangeCheckBox.getValue()));

    final Checkbox airHeatingCheckBox = new Checkbox("air heating");
    airHeatingCheckBox.setValue(ventModuleParamsDto.isAirHeatingEnabled());
    airHeatingCheckBox.addValueChangeListener(
        event -> ventModuleParamsDto.setAirHeatingEnabled(airHeatingCheckBox.getValue()));

    final Checkbox airCoolingCheckBox = new Checkbox("air cooling");
    airCoolingCheckBox.setValue(ventModuleParamsDto.isAirCoolingEnabled());
    airCoolingCheckBox.addValueChangeListener(
        event -> ventModuleParamsDto.setAirCoolingEnabled(airCoolingCheckBox.getValue()));

    final Checkbox airConditionCheckBox = new Checkbox("air condition");
    airConditionCheckBox.setValue(ventModuleParamsDto.isAirConditionEnabled());
    airConditionCheckBox.addValueChangeListener(
        event -> ventModuleParamsDto.setAirConditionEnabled(airConditionCheckBox.getValue()));

    final Checkbox nightHoursCheckBox = new Checkbox("night hours");
    nightHoursCheckBox.setValue(ventModuleParamsDto.isNightHoursEnabled());
    nightHoursCheckBox.addValueChangeListener(
        event -> ventModuleParamsDto.setNightHoursEnabled(nightHoursCheckBox.getValue()));

    final IntegerField inletFanNightHoursMaxPowerField =
        new PercentageField("inlet fan night hours max power");
    inletFanNightHoursMaxPowerField.setValue(ventModuleParamsDto.getInletFanNightHoursMaxPower());
    inletFanNightHoursMaxPowerField.addValueChangeListener(
        event ->
            ventModuleParamsDto.setInletFanNightHoursMaxPower(
                inletFanNightHoursMaxPowerField.getValue()));

    final IntegerField outletFanNightHoursMaxPowerField =
        new PercentageField("outlet fan night hours max power");
    outletFanNightHoursMaxPowerField.setValue(ventModuleParamsDto.getOutletFanNightHoursMaxPower());
    outletFanNightHoursMaxPowerField.addValueChangeListener(
        event ->
            ventModuleParamsDto.setOutletFanNightHoursMaxPower(
                outletFanNightHoursMaxPowerField.getValue()));

    final TimeRangesGrid nightHours =
        new TimeRangesGrid("Night hours", ventModuleParamsDto.getNightHours(), false);

    layout.add(
        fireplaceAirOverpressureCheckBox,
        fireplaceAirOverpressureLevelField,
        humidityAlertCheckBox,
        airExchangeCheckBox,
        airHeatingCheckBox,
        airCoolingCheckBox,
        airConditionCheckBox,
        nightHoursCheckBox,
        inletFanNightHoursMaxPowerField,
        outletFanNightHoursMaxPowerField,
        nightHours);

    return layout;
  }
}
