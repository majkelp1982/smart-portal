package pl.smarthouse.components.params;

import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.textfield.NumberField;

public class TemperatureField extends NumberField {
  public TemperatureField(final String label, final double min, final double max) {
    setLabel(label);
    setSuffixComponent(new NativeLabel("°C"));
    setMin(min);
    setMax(max);
    setStep(0.1);
    setStepButtonsVisible(true);
  }
}
