package pl.smarthouse.views.comfort;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
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
  private final HashMap<ZoneName, ValueContainer> valueContainerMap = new HashMap<>();
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
    overviewTab.add(createZoneOverview(zoneName, moduleDto));

    // Create tab for zone
    final VerticalLayout layout = new VerticalLayout();
    final Label label = new Label("Zone: " + zoneName);
    layout.add(label);

    tabs.add(zoneName.name(), layout);
  }

  private HorizontalLayout createZoneOverview(
      final ZoneName zoneName, final ComfortModuleDto comfortDto) {

    final Tile tile = new Tile("room.svg", zoneName.name());
    final Info temperature = new Info("temperatura", "°C");
    final Info humidity = new Info("wilgotność", "%");
    tile.getDetailsContainer().add(temperature.getLayout(), humidity.getLayout());

    // Values
    final ValueContainer valueContainer = new ValueContainer(comfortDto);
    valueContainer.put("sensor.temperature", temperature);
    valueContainer.put("sensor.humidity", humidity);

    valueContainerMap.put(zoneName, valueContainer);

    return tile;
  }

  private String cutNameIfNecessaryAndReturn(final String fullComfortName) {

    if (fullComfortName.contains("COMFORT")) {
      return fullComfortName.substring(8);
    } else {
      return fullComfortName;
    }
  }
}
