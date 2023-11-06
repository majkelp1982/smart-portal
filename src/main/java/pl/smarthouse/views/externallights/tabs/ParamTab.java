package pl.smarthouse.views.externallights.tabs;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import pl.smarthouse.components.params.PercentageField;
import pl.smarthouse.components.params.TimeRangesGrid;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.externallights.core.LightZoneParamDto;

public class ParamTab {

  public VerticalLayout get(final ExternalLightsModuleParamsDto externalLightsModuleParamsDto) {
    final VerticalLayout layout = new VerticalLayout();

    final Accordion accordion = new Accordion();
    accordion.add("entrance", zoneLightParamLayout(externalLightsModuleParamsDto.getEntrance()));
    accordion.add("driveway", zoneLightParamLayout(externalLightsModuleParamsDto.getDriveway()));
    accordion.add("carport", zoneLightParamLayout(externalLightsModuleParamsDto.getCarport()));
    accordion.add("garden", zoneLightParamLayout(externalLightsModuleParamsDto.getGarden()));

    final IntegerField lightIntenseThresholdFiled = new PercentageField("light intense threshold");
    lightIntenseThresholdFiled.setValue(externalLightsModuleParamsDto.getLightIntenseThreshold());
    lightIntenseThresholdFiled.addValueChangeListener(
        event ->
            externalLightsModuleParamsDto.setLightIntenseThreshold(
                lightIntenseThresholdFiled.getValue()));

    final TimeRangesGrid enableTimeRange =
        new TimeRangesGrid(
            "Enabled time range", externalLightsModuleParamsDto.getEnableTimeRange(), false);

    layout.add(accordion, lightIntenseThresholdFiled, enableTimeRange);
    return layout;
  }

  private VerticalLayout zoneLightParamLayout(final LightZoneParamDto lightZoneParamDto) {
    final VerticalLayout layout = new VerticalLayout();

    final IntegerField maxPowerFiled = new PercentageField("max power");
    maxPowerFiled.setValue(lightZoneParamDto.getMaxPower());
    maxPowerFiled.addValueChangeListener(
        event -> lightZoneParamDto.setMaxPower(maxPowerFiled.getValue()));

    final IntegerField standbyPowerFiled = new PercentageField("standby power");
    standbyPowerFiled.setValue(lightZoneParamDto.getStandByPower());
    standbyPowerFiled.addValueChangeListener(
        event -> lightZoneParamDto.setStandByPower(standbyPowerFiled.getValue()));

    layout.add(maxPowerFiled, standbyPowerFiled);
    return layout;
  }
}
