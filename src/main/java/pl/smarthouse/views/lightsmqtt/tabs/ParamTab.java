package pl.smarthouse.views.lightsmqtt.tabs;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import java.util.Arrays;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightZone;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightZoneParamsDto;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightsMqttParamDto;

public class ParamTab {

  public VerticalLayout get(final LightsMqttParamDto lightsMqttParamDto) {
    final VerticalLayout layout = new VerticalLayout();
    Arrays.stream(LightZone.values())
        .forEach(
            lightZone ->
                layout.add(
                    new Details(
                        lightZone.toString(),
                        zoneLightParamLayout(lightsMqttParamDto.getZoneParams().get(lightZone)))));
    return layout;
  }

  private VerticalLayout zoneLightParamLayout(final LightZoneParamsDto lightZoneParamsDto) {
    final VerticalLayout layout = new VerticalLayout();

    final IntegerField standbyField = new StandbyField();
    standbyField.setValue(lightZoneParamsDto.getStandby());
    standbyField.addValueChangeListener(
        event -> lightZoneParamsDto.setStandby(standbyField.getValue()));

    final IntegerField colorTemperatureField = new ColorTemperature();
    colorTemperatureField.setValue(lightZoneParamsDto.getColorTemperature());
    colorTemperatureField.addValueChangeListener(
        event -> lightZoneParamsDto.setColorTemperature(colorTemperatureField.getValue()));

    layout.add(standbyField, colorTemperatureField);
    return layout;
  }

  private static class StandbyField extends IntegerField {
    public StandbyField() {
      setLabel("standby power (1-254)");
      setMin(1);
      setMax(254);
      setStep(1);
      setStepButtonsVisible(true);
    }
  }

  private static class ColorTemperature extends IntegerField {
    public ColorTemperature() {
      setLabel("color temperature (50-500");
      setMin(50);
      setMax(500);
      setStep(1);
      setStepButtonsVisible(true);
    }
  }
}
