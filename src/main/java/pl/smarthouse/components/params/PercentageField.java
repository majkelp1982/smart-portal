package pl.smarthouse.components.params;

import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.textfield.IntegerField;

public class PercentageField extends IntegerField {
  public PercentageField(final String label) {
    setLabel(label);
    setSuffixComponent(new NativeLabel("%"));
    setMin(1);
    setMax(100);
    setStep(1);
    setStepButtonsVisible(true);
  }
}
