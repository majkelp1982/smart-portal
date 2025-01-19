package pl.smarthouse.views.chillzone.tabs;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import pl.smarthouse.components.params.PercentageField;
import pl.smarthouse.components.params.TemperatureField;
import pl.smarthouse.sharedobjects.dto.chillzone.ChillZoneParamModuleDto;
import pl.smarthouse.sharedobjects.dto.chillzone.SpaDeviceParam;

public class ParamTab {

  public VerticalLayout get(final ChillZoneParamModuleDto chillZoneParamModuleDto) {
    final VerticalLayout layout = new VerticalLayout();
    layout.add(spaDeviceParamDetails("Sauna", chillZoneParamModuleDto.getSauna()));
    layout.add(spaDeviceParamDetails("Chill room", chillZoneParamModuleDto.getChillRoom()));
    return layout;
  }

  private Details spaDeviceParamDetails(String spaDeviceName, SpaDeviceParam spaDeviceParam) {
    VerticalLayout layout = new VerticalLayout();

    final Checkbox enabledCheckBox = new Checkbox("enabled");
    enabledCheckBox.setValue(spaDeviceParam.isEnabled());
    enabledCheckBox.addValueChangeListener(
        event -> spaDeviceParam.setEnabled(enabledCheckBox.getValue()));

    final NumberField minRequiredTemperatureField =
        new TemperatureField("min required temperature", 1, 10.0);
    minRequiredTemperatureField.setStep(1.0d);
    minRequiredTemperatureField.setValue(spaDeviceParam.getMinRequiredTemperature());
    minRequiredTemperatureField.addValueChangeListener(
        event -> spaDeviceParam.setMinRequiredTemperature(minRequiredTemperatureField.getValue()));

    final NumberField requiredTemperatureField =
        new TemperatureField("required temperature", 1, 90.0);
    requiredTemperatureField.setStep(1.0d);
    requiredTemperatureField.setValue(spaDeviceParam.getRequiredTemperature());
    requiredTemperatureField.addValueChangeListener(
        event -> spaDeviceParam.setRequiredTemperature(requiredTemperatureField.getValue()));

    final IntegerField holdTimeField = new PercentageField("hold time");
    holdTimeField.setSuffixComponent(new Label("minutes"));
    holdTimeField.setMin(5);
    holdTimeField.setMax(180);
    holdTimeField.setStep(5);
    holdTimeField.setStepButtonsVisible(true);
    holdTimeField.setValue(spaDeviceParam.getHoldTimeInMinutes());
    holdTimeField.addValueChangeListener(
        event -> spaDeviceParam.setHoldTimeInMinutes(holdTimeField.getValue()));
    layout.add(
        enabledCheckBox, minRequiredTemperatureField, requiredTemperatureField, holdTimeField);
    return new Details(spaDeviceName, layout);
  }
}
