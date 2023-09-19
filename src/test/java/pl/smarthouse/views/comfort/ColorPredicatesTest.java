package pl.smarthouse.views.comfort;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.smarthouse.components.Info;
import pl.smarthouse.model.ComponentColor;

class ColorPredicatesTest {

  @Test
  void givenColdTemp_whenSetValue_thenColorCold() {
    final Info temperature = new Info("test");
    ColorPredicates.assignToTemperature(temperature, -0.5, 0.2, 0.3);

    temperature.setExpectedValue(10);
    temperature.setValue(9.5);

    Assertions.assertEquals(
        ComponentColor.COLD.value, temperature.getComponent().getStyle().get("color"));
  }

  @Test
  void givenUnderTemp_whenSetValue_thenColorUnder() {
    final Info temperature = new Info("test");
    ColorPredicates.assignToTemperature(temperature, -0.5, 0.2, 0.3);

    temperature.setExpectedValue(10);
    temperature.setValue(9.6);

    Assertions.assertEquals(
        ComponentColor.UNDER.value, temperature.getComponent().getStyle().get("color"));
  }

  @Test
  void givenGoodTemp_whenSetValue_thenColorGood() {
    final Info temperature = new Info("test");
    ColorPredicates.assignToTemperature(temperature, -0.5, 0.2, 0.3);

    temperature.setExpectedValue(10);
    temperature.setValue(9.9);
    Assertions.assertEquals(
        ComponentColor.GOOD.value, temperature.getComponent().getStyle().get("color"));
  }

  @Test
  void givenGoodTemp_whenSetValue_thenColorGood1() {
    final Info temperature = new Info("test");
    ColorPredicates.assignToTemperature(temperature, -0.5, 0.2, 0.3);

    temperature.setExpectedValue(10);
    temperature.setValue(10.2);
    Assertions.assertEquals(
        ComponentColor.GOOD.value, temperature.getComponent().getStyle().get("color"));
  }

  @Test
  void givenOverTemp_whenSetValue_thenColorOver() {
    final Info temperature = new Info("test");
    ColorPredicates.assignToTemperature(temperature, -0.5, 0.2, 0.4);

    temperature.setExpectedValue(10);
    temperature.setValue(10.3);

    Assertions.assertEquals(
        ComponentColor.OVER.value, temperature.getComponent().getStyle().get("color"));
  }

  @Test
  void givenHotTemp_whenSetValue_thenColorHot() {
    final Info temperature = new Info("test");
    ColorPredicates.assignToTemperature(temperature, -0.5, 0.2, 0.3);

    temperature.setExpectedValue(10);
    temperature.setValue(10.4);

    Assertions.assertEquals(
        ComponentColor.HOT.value, temperature.getComponent().getStyle().get("color"));
  }
}
