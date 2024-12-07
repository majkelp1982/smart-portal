package pl.smarthouse.views.lightsmqtt.tabs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.smarthouse.service.WebService;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.*;

@RequiredArgsConstructor
public class OverviewTab {
  private static final Logger log = LoggerFactory.getLogger(OverviewTab.class);
  private final WebService webService;
  private final LightsMqttDto lightsMqttDto;
  private final LightsMqttParamDto lightsMqttParamDto;
  private final Grid<Map.Entry<LightZone, ZoneState>> requireZoneStatesGrid = new Grid<>();
  private final Grid<Light> lightsGrid = new Grid<>();
  private final Grid<MotionSensor> motionSensorsGrid = new Grid<>();

  public VerticalLayout get() {
    final VerticalLayout overviewTab = new VerticalLayout();
    H4 requireZoneStateLabel = new H4("Require zone state");
    prepareRequireZoneStatesGrid();

    H4 lightsLabel = new H4("Lights");
    prepareLightGrid();

    H4 motionSensorsLabel = new H4("Motion sensors");
    prepareMotionSensorsGrid();

    overviewTab.add(
        requireZoneStateLabel,
        requireZoneStatesGrid,
        lightsLabel,
        lightsGrid,
        motionSensorsLabel,
        motionSensorsGrid);
    return overviewTab;
  }

  public void refreshDetails(LightsMqttDto lightsMqttDto) {
    lightsGrid.setItems(lightsMqttDto.getLights().values());
    lightsGrid.setHeightFull();
    lightsGrid.setAllRowsVisible(true);
    requireZoneStatesGrid.setItems(lightsMqttDto.getRequireZoneStates().entrySet());
    requireZoneStatesGrid.setHeightFull();
    requireZoneStatesGrid.setAllRowsVisible(true);
    motionSensorsGrid.setItems(lightsMqttDto.getMotionSensors().values());
    motionSensorsGrid.setHeightFull();
    motionSensorsGrid.setAllRowsVisible(true);
  }

  private void prepareRequireZoneStatesGrid() {
    requireZoneStatesGrid.removeAllColumns();

    requireZoneStatesGrid.addColumn(Map.Entry::getKey).setKey("Zone").setHeader("Zone");
    requireZoneStatesGrid
        .addColumn(
            new ComponentRenderer<>(
                entry -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final Mode currentMode =
                      entry.getValue().getMode() == null ? Mode.AUTO : entry.getValue().getMode();
                  final Button button = new Button(currentMode.name());
                  button.addClickListener(
                      buttonClickEvent -> {
                        Mode nextMode = Mode.getNext(Mode.valueOf(button.getText()));
                        if (Mode.TRIGGER.equals(nextMode)) {
                          nextMode = Mode.getNext(nextMode);
                        }
                        log.info("Zone: {}, mode changed to: {}", entry.getKey(), nextMode);
                        button.setText(nextMode.name());
                        entry.getValue().setMode(nextMode);
                        webService
                            .patch(
                                constructLightModeUrl(entry.getKey(), nextMode), String.class, "")
                            .subscribe();
                      });
                  layout.add(button);
                  return layout;
                }))
        .setHeader("Mode");

    requireZoneStatesGrid
        .addColumn(requireZoneStatesEntry -> requireZoneStatesEntry.getValue().getState())
        .setKey("State")
        .setHeader("State");
    requireZoneStatesGrid
        .addColumn(requireZoneStatesEntry -> requireZoneStatesEntry.getValue().getBrightness())
        .setKey("Brightness")
        .setHeader("Brightness");
    requireZoneStatesGrid
        .addColumn(
            requireZoneStatesEntry -> requireZoneStatesEntry.getValue().getColorTemperature())
        .setKey("Color temp")
        .setHeader("Color temp");
    requireZoneStatesGrid
        .addColumn(
            requireZoneStatesEntry -> {
              int holdTime =
                  lightsMqttParamDto
                      .getZoneParams()
                      .get(requireZoneStatesEntry.getKey())
                      .getHoldTime();
              long timeLeft =
                  Duration.between(
                          LocalDateTime.now(),
                          requireZoneStatesEntry
                              .getValue()
                              .getTriggerTimeStamp()
                              .plusMinutes(holdTime))
                      .toMinutes();
              if (timeLeft < 0) {
                timeLeft = 0;
              }
              return timeLeft;
            })
        .setKey("Hold time left")
        .setHeader("Hold time left[min]");
    requireZoneStatesGrid
        .getColumns()
        .forEach(
            column -> {
              column.setResizable(true);
              column.setSortable(true);
              column.setAutoWidth(true);
            });
    requireZoneStatesGrid.setPageSize(1000);
    requireZoneStatesGrid.setAllRowsVisible(true);
    requireZoneStatesGrid.setMultiSort(true);
    requireZoneStatesGrid.recalculateColumnWidths();
    requireZoneStatesGrid.sort(
        Collections.singletonList(
            new GridSortOrder<>(
                requireZoneStatesGrid.getColumnByKey("Zone"), SortDirection.ASCENDING)));
  }

  private void prepareMotionSensorsGrid() {
    motionSensorsGrid.removeAllColumns();
    motionSensorsGrid.addColumn(MotionSensor::getLightZones).setKey("Zones").setHeader("Zones");
    motionSensorsGrid
        .addColumn(motionSensor -> motionSensor.getSensor().isOccupancy())
        .setKey("Occupancy")
        .setHeader("Occupancy");
    motionSensorsGrid
        .addColumn(motionSensor -> motionSensor.getSensor().isBatteryLow())
        .setKey("Low battery")
        .setHeader("Low battery");
    motionSensorsGrid
        .addColumn(motionSensor -> motionSensor.getSensor().getBattery())
        .setKey("Battery")
        .setHeader("Battery [%]");
    motionSensorsGrid
        .addColumn(motionSensor -> motionSensor.getSensor().getVoltage() / 1000.00d)
        .setKey("Voltage")
        .setHeader("Voltage [V]");
    motionSensorsGrid
        .addColumn(motionSensor -> motionSensor.getSensor().getIeeeAddress())
        .setKey("Address")
        .setHeader("Address");
    motionSensorsGrid
        .addColumn(motionSensor -> motionSensor.getSensor().getLinkQuality())
        .setKey("link quality")
        .setHeader("link quality");
    motionSensorsGrid
        .addColumn(motionSensor -> motionSensor.getSensor().getLastUpdateString())
        .setKey("Last update")
        .setHeader("Last update");
    motionSensorsGrid
        .getColumns()
        .forEach(
            column -> {
              column.setResizable(true);
              column.setSortable(true);
              column.setAutoWidth(true);
            });
    motionSensorsGrid.setPageSize(1000);
    motionSensorsGrid.setAllRowsVisible(true);
    motionSensorsGrid.setMultiSort(true);
    motionSensorsGrid.recalculateColumnWidths();
  }

  private void prepareLightGrid() {
    lightsGrid.removeAllColumns();
    lightsGrid.addColumn(Light::getLightZone).setKey("Zone").setHeader("Zone");
    lightsGrid
        .addColumn(light -> light.getLightDevice().getState())
        .setKey("State")
        .setHeader("State");
    lightsGrid
        .addColumn(light -> light.getLightDevice().getBrightness())
        .setKey("Brightness")
        .setHeader("Brightness");
    lightsGrid
        .addColumn(light -> light.getLightDevice().getColorTemperature())
        .setKey("Color temp")
        .setHeader("Color temp");
    lightsGrid
        .addColumn(light -> light.getLightDevice().getIeeeAddress())
        .setKey("Address")
        .setHeader("Address");
    lightsGrid
        .addColumn(light -> light.getLightDevice().getLinkQuality())
        .setKey("link quality")
        .setHeader("link quality");
    lightsGrid
        .addColumn(light -> light.getLightDevice().getLastUpdateString())
        .setKey("Last update")
        .setHeader("Last update");
    lightsGrid
        .getColumns()
        .forEach(
            column -> {
              column.setResizable(true);
              column.setSortable(true);
              column.setAutoWidth(true);
            });
    lightsGrid.setPageSize(1000);
    lightsGrid.setAllRowsVisible(true);
    lightsGrid.setMultiSort(true);
    lightsGrid.recalculateColumnWidths();
  }

  private String constructLightModeUrl(LightZone lightZone, Mode mode) {
    return String.format(
        "http://%s/%s/setMode?mode=%s", lightsMqttDto.getServiceAddress(), lightZone, mode);
  }
}
