package pl.smarthouse.views.fireplace.tabs;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import pl.smarthouse.components.params.TemperatureField;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleParamsDto;

public class ParamTab {

  public VerticalLayout get(final FireplaceModuleParamsDto fireplaceModuleParamsDto) {
    final VerticalLayout layout = new VerticalLayout();

    final NumberField workingTemperatureFiled = new TemperatureField("working temperature", 50, 70);
    workingTemperatureFiled.setStep(1.0);
    workingTemperatureFiled.setValue(fireplaceModuleParamsDto.getWorkingTemperature());
    workingTemperatureFiled.addValueChangeListener(
        event ->
            fireplaceModuleParamsDto.setWorkingTemperature(workingTemperatureFiled.getValue()));

    final NumberField warningTemperatureFiled = new TemperatureField("warning temperature", 60, 90);
    warningTemperatureFiled.setStep(1.0);
    warningTemperatureFiled.setValue(fireplaceModuleParamsDto.getWarningTemperature());
    warningTemperatureFiled.addValueChangeListener(
        event ->
            fireplaceModuleParamsDto.setWarningTemperature(warningTemperatureFiled.getValue()));

    final NumberField alarmTemperatureFiled = new TemperatureField("alarm temperature", 60, 90);
    alarmTemperatureFiled.setStep(1.0);
    alarmTemperatureFiled.setValue(fireplaceModuleParamsDto.getAlarmTemperature());
    alarmTemperatureFiled.addValueChangeListener(
        event -> fireplaceModuleParamsDto.setAlarmTemperature(alarmTemperatureFiled.getValue()));

    layout.add(workingTemperatureFiled, warningTemperatureFiled, alarmTemperatureFiled);
    return layout;
  }
}
