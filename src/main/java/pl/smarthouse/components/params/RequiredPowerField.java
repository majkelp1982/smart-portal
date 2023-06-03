package pl.smarthouse.components.params;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.IntegerField;

public class RequiredPowerField extends IntegerField {
  public RequiredPowerField() {
    setLabel("required power");
    setSuffixComponent(new Label("%"));
    setMin(1);
    setMax(100);
    setStep(1);
    setStepButtonsVisible(true);
  }
}
