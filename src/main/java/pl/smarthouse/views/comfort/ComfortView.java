package pl.smarthouse.views.comfort;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.service.GuiService;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.views.MainView;

@PageTitle("Smart Portal | Comfort")
@Route(value = "Comfort", layout = MainView.class)
public class ComfortView extends VerticalLayout {
  private final GuiService guiService;
  private final HashMap<String, ValueContainer> valueContainerMap = new HashMap<>();
  TabSheet tabs;
  HorizontalLayout overviewTab;

  public ComfortView(@Autowired final GuiService guiService) {
    this.guiService = guiService;
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              valueContainerMap.values().stream()
                  .forEach(valueContainer -> valueContainer.updateValues());
            });
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    createView();
    UI.getCurrent().setPollInterval(1000);
  }

  @Override
  protected void onDetach(final DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    UI.getCurrent().setPollInterval(-1);
  }

  private void createView() {
    tabs = new TabSheet();
    tabs.add("Overview", overviewTab());
    add(tabs);

    guiService.getModuleDtos().stream()
        .filter(moduleDto -> moduleDto.getModuleName().contains("COMFORT"))
        .map(moduleDto -> (ComfortModuleDto) moduleDto)
        .forEach(this::createZone);
  }

  private HorizontalLayout overviewTab() {
    overviewTab = new HorizontalLayout();
    return overviewTab;
  }

  private void createZone(final ComfortModuleDto moduleDto) {

    final ZoneName zoneName =
        ZoneName.valueOf(cutNameIfNecessaryAndReturn(moduleDto.getModuleName()));

    // Add zone to overview
    overviewTab.add(createZoneOverview(zoneName.name(), zoneName.name(), moduleDto));

    // Create tab for zone
    tabs.add(zoneName.name(), createZoneTab(zoneName.name(), moduleDto));
  }

  private Tile createZoneOverview(
      final String zoneName, final String valueContainerName, final ComfortModuleDto comfortDto) {

    final Tile tile = new Tile("room.svg", zoneName);
    final Info temperature = new Info("temperatura", "°C");
    final Info humidity = new Info("wilgotność", "%");
    final Info currentOperation = new Info("operacja");
    final Info requiredPower = new Info("moc", "%");
    tile.getDetailsContainer()
        .add(
            temperature.getLayout(),
            humidity.getLayout(),
            currentOperation.getLayout(),
            requiredPower.getLayout());

    // Values
    final ValueContainer valueContainer = new ValueContainer(comfortDto);
    valueContainer.put("sensorResponse.temperature", temperature);
    valueContainer.put("sensorResponse.humidity", humidity);
    valueContainer.put("currentOperation", currentOperation);
    valueContainer.put("requiredPower", requiredPower);

    valueContainerMap.put(valueContainerName, valueContainer);

    return tile;
  }

  private VerticalLayout createZoneTab(final String zoneName, final ComfortModuleDto comfortDto) {
    final VerticalLayout layout = new VerticalLayout();
    final String zoneTabName = zoneName + "Tab";

    final Tile overviewTile = createZoneOverview(zoneName, zoneTabName, comfortDto);

    layout.add(enrichZoneOverviewWithDetails(zoneTabName, overviewTile));
    return layout;
  }

  private HorizontalLayout enrichZoneOverviewWithDetails(
      final String valueContainerName, final Tile zoneTabTile) {
    final VerticalLayout detailsContainer = zoneTabTile.getDetailsContainer();

    final Info leftHoldTimeInMinutes = new Info("podtrzymanie", "min");
    final Info sensorResponseUpdateTimestamp = new Info("update");
    final Info sensorError = new Info("sensor error");
    detailsContainer.add(
        leftHoldTimeInMinutes.getLayout(),
        sensorResponseUpdateTimestamp.getLayout(),
        sensorError.getLayout());

    // Values
    final ValueContainer valueContainer = valueContainerMap.get(valueContainerName);
    valueContainer.put("leftHoldTimeInMinutes", leftHoldTimeInMinutes);
    valueContainer.put("sensorResponse.!responseUpdate", sensorResponseUpdateTimestamp);
    valueContainer.put("sensorResponse.error", sensorError);

    return zoneTabTile;
  }

  private String cutNameIfNecessaryAndReturn(final String fullComfortName) {

    if (fullComfortName.contains("COMFORT")) {
      return fullComfortName.substring(8);
    } else {
      return fullComfortName;
    }
  }
}
