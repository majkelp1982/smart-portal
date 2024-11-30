package pl.smarthouse.views.lightsmqtt.tabs;

import static pl.smarthouse.properties.SupportedZigbeeDevicesProperties.SUPPORTED_LIGHTS;
import static pl.smarthouse.properties.SupportedZigbeeDevicesProperties.SUPPORTED_MOTION_SENSORS;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.smarthouse.components.params.TimeRangesGrid;
import pl.smarthouse.properties.SupportedZigbeeDevicesProperties;
import pl.smarthouse.service.WebService;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightZone;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightZoneParamsDto;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightsMqttParamDto;
import pl.smarthouse.sharedobjects.dto.zigbee.ZigbeeDevice;

@RequiredArgsConstructor
public class ParamTab {
  public static final String NOT_APPLICABLE = "---";
  private final WebService webService;
  private final LightsMqttParamDto lightsMqttParamDto;
  final String serviceAddress;
  private final List<ZigbeeDeviceWithLightZone> allZigbeeDevices = new ArrayList<>();
  private final Grid<ZigbeeDeviceWithLightZone> zigbeeDevicesGrid = new Grid<>();
  private final Set<String> availableZones = new HashSet<>();

  public VerticalLayout get() {
    prepareAvailableZones();
    collectAllZigbeeDevicesAndEnrichWithLightZone(serviceAddress);
    prepareZigbeeDevicesGrid();
    final VerticalLayout layout = new VerticalLayout();
    Arrays.stream(LightZone.values())
        .forEach(
            lightZone ->
                layout.add(
                    new Details(
                        lightZone.toString(),
                        zoneLightParamLayout(lightsMqttParamDto.getZoneParams().get(lightZone)))));

    final TimeRangesGrid enableTimeRange =
        new TimeRangesGrid("Enabled time range", lightsMqttParamDto.getEnableTimeRange(), false);
    layout.add(enableTimeRange, zigbeeDevicesGrid);

    return layout;
  }

  private void prepareAvailableZones() {
    Arrays.stream(LightZone.values())
        .forEach(lightZone -> availableZones.add(lightZone.toString()));
    availableZones.add(NOT_APPLICABLE);
  }

  private void collectAllZigbeeDevicesAndEnrichWithLightZone(final String serviceAddress) {
    allZigbeeDevices.clear();
    webService
        .get(prepareGetAllDevicesUrl(serviceAddress), ZigbeeDeviceWithLightZone.class)
        .doOnNext(
            zigbeeDevice -> {
              if (zigbeeDevice.getDefinition() != null) {
                allZigbeeDevices.add(zigbeeDevice);
              }
            })
        .doOnNext(this::enrichWithLightZone)
        .subscribe();
  }

  private void enrichWithLightZone(ZigbeeDeviceWithLightZone zigbeeDeviceWithLightZone) {
    String deviceAddress = zigbeeDeviceWithLightZone.getIeeeAddress();
    Optional.ofNullable(lightsMqttParamDto.getLights().get(deviceAddress))
        .ifPresentOrElse(
            lightZone -> zigbeeDeviceWithLightZone.setLightZone(lightZone.name()),
            () ->
                Optional.ofNullable(lightsMqttParamDto.getMotionSensors().get(deviceAddress))
                    .ifPresentOrElse(
                        lightZone -> zigbeeDeviceWithLightZone.setLightZone(lightZone.name()),
                        () -> zigbeeDeviceWithLightZone.setLightZone(NOT_APPLICABLE)));
  }

  private String prepareGetAllDevicesUrl(String serviceAddress) {
    return serviceAddress.substring(0, serviceAddress.indexOf("/")) + "/all";
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

    final IntegerField holdTimeField = new IntegerField();
    holdTimeField.setLabel("hold time[min] 1-10");
    holdTimeField.setMin(1);
    holdTimeField.setMax(10);
    holdTimeField.setStep(1);
    holdTimeField.setStepButtonsVisible(true);
    holdTimeField.setValue(lightZoneParamsDto.getHoldTime());
    holdTimeField.addValueChangeListener(
        event -> lightZoneParamsDto.setHoldTime(holdTimeField.getValue()));
    layout.add(standbyField, colorTemperatureField, holdTimeField);
    return layout;
  }

  private void prepareZigbeeDevicesGrid() {
    zigbeeDevicesGrid.removeAllColumns();
    zigbeeDevicesGrid
        .addColumn(zigbeeDevice -> zigbeeDevice.getDefinition().getModel())
        .setKey("Model")
        .setHeader("model");
    zigbeeDevicesGrid
        .addColumn(zigbeeDevice -> zigbeeDevice.getDefinition().getDescription())
        .setKey("Description")
        .setHeader("description");
    zigbeeDevicesGrid
        .addColumn(ZigbeeDevice::getIeeeAddress)
        .setKey("Address")
        .setHeader("address");
    zigbeeDevicesGrid
        .addColumn(
            new ComponentRenderer<>(
                zigbeeDeviceWithLightZone -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final Select<String> select = new Select<>();
                  select.setItems(availableZones);
                  select.setValue(zigbeeDeviceWithLightZone.getLightZone());
                  select.addValueChangeListener(
                      event -> {
                        if (isZigbeeDeviceSupported(zigbeeDeviceWithLightZone)) {
                          zigbeeDeviceWithLightZone.setLightZone(event.getValue());
                        } else {
                          select.setValue(NOT_APPLICABLE);
                        }
                        assignLightsAndMotionSensors();
                      });
                  layout.add(select);
                  return layout;
                }))
        .setHeader("Light zone");

    zigbeeDevicesGrid
        .getColumns()
        .forEach(
            column -> {
              column.setResizable(true);
              column.setSortable(true);
              column.setAutoWidth(true);
            });
    zigbeeDevicesGrid.setPageSize(1000);
    zigbeeDevicesGrid.setAllRowsVisible(true);
    zigbeeDevicesGrid.setMultiSort(true);
    zigbeeDevicesGrid.recalculateColumnWidths();

    zigbeeDevicesGrid.setItems(allZigbeeDevices);
    zigbeeDevicesGrid.setHeightFull();
    zigbeeDevicesGrid.setAllRowsVisible(true);
  }

  private void assignLightsAndMotionSensors() {
    List<ZigbeeDeviceWithLightZone> zigbeeDevices =
        zigbeeDevicesGrid
            .getDataProvider()
            .fetch(new Query<>())
            .filter(
                zigbeeDeviceWithLightZone ->
                    !zigbeeDeviceWithLightZone.getLightZone().equals(NOT_APPLICABLE))
            .toList();
    zigbeeDevices.stream()
        .filter(
            zigbeeDeviceWithLightZone ->
                SupportedZigbeeDevicesProperties.SUPPORTED_LIGHTS.contains(
                    zigbeeDeviceWithLightZone.getDefinition().getModel()))
        .forEach(
            zigbeeDeviceWithLightZone -> {
              if (lightsMqttParamDto.getLights() == null) {
                lightsMqttParamDto.setLights(new HashMap<>());
              }

              lightsMqttParamDto
                  .getLights()
                  .put(
                      zigbeeDeviceWithLightZone.getIeeeAddress(),
                      LightZone.valueOf(zigbeeDeviceWithLightZone.getLightZone()));
            });
    zigbeeDevices.stream()
        .filter(
            zigbeeDeviceWithLightZone ->
                SUPPORTED_MOTION_SENSORS.contains(
                    zigbeeDeviceWithLightZone.getDefinition().getModel()))
        .forEach(
            zigbeeDeviceWithLightZone -> {
              if (lightsMqttParamDto.getMotionSensors() == null) {
                lightsMqttParamDto.setMotionSensors(new HashMap<>());
              }

              lightsMqttParamDto
                  .getMotionSensors()
                  .put(
                      zigbeeDeviceWithLightZone.getIeeeAddress(),
                      LightZone.valueOf(zigbeeDeviceWithLightZone.getLightZone()));
            });
  }

  private boolean isZigbeeDeviceSupported(ZigbeeDeviceWithLightZone zigbeeDevices) {
    var model = zigbeeDevices.getDefinition().getModel();
    if (!SUPPORTED_MOTION_SENSORS.contains(model) && !SUPPORTED_LIGHTS.contains(model)) {
      Notification notification =
          new Notification(
              String.format("Device not supported: %s", zigbeeDevices.getDefinition()));
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setDuration(10000);
      notification.open();
      return false;
    }
    return true;
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

  @Setter
  @Getter
  private static class ZigbeeDeviceWithLightZone extends ZigbeeDevice {
    private String lightZone;
  }
}
