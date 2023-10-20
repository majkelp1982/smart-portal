package pl.smarthouse.views.comfort.subview;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import lombok.experimental.UtilityClass;
import pl.smarthouse.components.params.PercentageField;
import pl.smarthouse.sharedobjects.dto.comfort.core.HumidityAlert;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.FunctionType;

@UtilityClass
public class HumidityAlertView {
  public void addForm(
      final Accordion accordion,
      final FunctionType functionType,
      final HumidityAlert humidityAlert) {
    if (functionType != null && !FunctionType.AIR_EXTRACT.equals(functionType)) {
      return;
    }
    validateAirExchanger(humidityAlert);
    final VerticalLayout layout = new VerticalLayout();

    final Checkbox enabledCheckBox = new Checkbox("enabled");
    enabledCheckBox.setValue(humidityAlert.isEnabled());
    enabledCheckBox.addValueChangeListener(
        event -> humidityAlert.setEnabled(enabledCheckBox.getValue()));

    final IntegerField maxHumidityField = new PercentageField("max humidity");
    maxHumidityField.setValue(humidityAlert.getMaxHumidity());
    maxHumidityField.addValueChangeListener(
        event -> humidityAlert.setMaxHumidity(maxHumidityField.getValue()));

    final IntegerField requiredPowerField = new PercentageField("required power");
    requiredPowerField.setValue(humidityAlert.getRequiredPower());
    requiredPowerField.addValueChangeListener(
        event -> humidityAlert.setRequiredPower(requiredPowerField.getValue()));

    final IntegerField requiredTurboPowerField = new PercentageField("required turbo power");
    requiredTurboPowerField.setValue(humidityAlert.getRequiredTurboPower());
    requiredTurboPowerField.addValueChangeListener(
        event -> humidityAlert.setRequiredTurboPower(requiredTurboPowerField.getValue()));

    final IntegerField holdTimeField = new PercentageField("hold time");
    holdTimeField.setSuffixComponent(new Label("minutes"));
    holdTimeField.setMin(1);
    holdTimeField.setMax(30);
    holdTimeField.setStep(1);
    holdTimeField.setStepButtonsVisible(true);
    holdTimeField.setValue(humidityAlert.getHoldTimeInMinutes());
    holdTimeField.addValueChangeListener(
        event -> humidityAlert.setHoldTimeInMinutes(holdTimeField.getValue()));

    layout.add(
        enabledCheckBox,
        maxHumidityField,
        requiredPowerField,
        requiredTurboPowerField,
        holdTimeField);
    accordion.add("Humidity alert", layout);
  }

  private void validateAirExchanger(final HumidityAlert humidityAlert) {
    return;
  }
}
