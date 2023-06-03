package pl.smarthouse.views.comfort.subview;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import java.util.HashSet;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import pl.smarthouse.components.params.RequiredPowerField;
import pl.smarthouse.components.params.TimeRangesGrid;
import pl.smarthouse.sharedobjects.dto.comfort.core.AirExchanger;

@UtilityClass
public class AirExchangerView {
  public void addForm(final Accordion accordion, final AirExchanger airExchanger) {
    validateAirExchanger(airExchanger);
    final VerticalLayout layout = new VerticalLayout();

    final Checkbox enabledCheckBox = new Checkbox("enabled");
    enabledCheckBox.setValue(airExchanger.isEnabled());
    enabledCheckBox.addValueChangeListener(
        event -> airExchanger.setEnabled(enabledCheckBox.getValue()));

    final IntegerField requiredPowerField = new RequiredPowerField();
    requiredPowerField.setValue(airExchanger.getRequiredPower());
    requiredPowerField.addValueChangeListener(
        event -> airExchanger.setRequiredPower(requiredPowerField.getValue()));

    final TimeRangesGrid workDayGrid =
        new TimeRangesGrid("Workday time ranges", airExchanger.getWorkdayTimeRanges());
    workDayGrid.setItems(airExchanger.getWorkdayTimeRanges());

    final TimeRangesGrid weekendGrid =
        new TimeRangesGrid("Weekend time ranges", airExchanger.getWeekendTimeRanges());
    workDayGrid.setItems(airExchanger.getWeekendTimeRanges());

    layout.add(enabledCheckBox, requiredPowerField, workDayGrid, weekendGrid);
    accordion.add("Air exchanger", layout);
  }

  private void validateAirExchanger(final AirExchanger airExchanger) {
    if (Objects.isNull(airExchanger.getWorkdayTimeRanges())) {
      airExchanger.setWorkdayTimeRanges(new HashSet<>());
    }
    if (Objects.isNull(airExchanger.getWeekendTimeRanges())) {
      airExchanger.setWeekendTimeRanges(new HashSet<>());
    }
  }
}
