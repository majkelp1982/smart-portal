package pl.smarthouse.views.lightsmqtt.tabs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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
  private final Grid<Map.Entry<LightZone, ZoneState>> requireZoneStatesGrid = new Grid<>();
  private final Grid<Light> lightGrid = new Grid<>();

  public VerticalLayout get() {
    final VerticalLayout overviewTab = new VerticalLayout();
    prepareRequireZoneStatesGrid();
    prepareLightGrid();

    overviewTab.add(requireZoneStatesGrid, lightGrid);
    return overviewTab;
  }

  public void refreshDetails(LightsMqttDto lightsMqttDto) {
    lightGrid.setItems(lightsMqttDto.getLights().values());
    lightGrid.setHeightFull();
    lightGrid.setAllRowsVisible(true);
    requireZoneStatesGrid.setItems(lightsMqttDto.getRequireZoneStates().entrySet());
    requireZoneStatesGrid.setHeightFull();
    requireZoneStatesGrid.setAllRowsVisible(true);
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

  private void prepareLightGrid() {
    lightGrid.removeAllColumns();
    lightGrid.addColumn(Light::getLightZone).setKey("Zone").setHeader("Zone");
    lightGrid
        .addColumn(light -> light.getLightDevice().getState())
        .setKey("State")
        .setHeader("State");
    lightGrid
        .addColumn(light -> light.getLightDevice().getBrightness())
        .setKey("Brightness")
        .setHeader("Brightness");
    lightGrid
        .addColumn(light -> light.getLightDevice().getColorTemperature())
        .setKey("Color temp")
        .setHeader("Color temp");
    lightGrid
        .addColumn(light -> light.getLightDevice().getIeeeAddress())
        .setKey("Address")
        .setHeader("Address");
    lightGrid
        .addColumn(light -> light.getLightDevice().getLinkQuality())
        .setKey("link quality")
        .setHeader("link quality");
    lightGrid
        .addColumn(light -> light.getLightDevice().getLastUpdateString())
        .setKey("Last update")
        .setHeader("Last update");
    lightGrid
        .getColumns()
        .forEach(
            column -> {
              column.setResizable(true);
              column.setSortable(true);
              column.setAutoWidth(true);
            });
    lightGrid.setPageSize(1000);
    lightGrid.setAllRowsVisible(true);
    lightGrid.setMultiSort(true);
    lightGrid.recalculateColumnWidths();
    lightGrid.sort(
        Collections.singletonList(
            new GridSortOrder<>(lightGrid.getColumnByKey("Zone"), SortDirection.ASCENDING)));
  }

  private String constructLightModeUrl(LightZone lightZone, Mode mode) {
    return String.format(
        "http://%s/%s/setMode?mode=%s", lightsMqttDto.getServiceAddress(), lightZone, mode);
  }
}
