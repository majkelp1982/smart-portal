package pl.smarthouse.views.comfort;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.Tile;
import pl.smarthouse.service.GuiService;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.views.MainView;

@PageTitle("Smart Portal | Comfort")
@Route(value = "Comfort", layout = MainView.class)
@Service
public class ComfortView extends VerticalLayout {
  private final GuiService guiService;
  TabSheet tabs;
  HorizontalLayout overviewTab;

  public ComfortView(@Autowired final GuiService guiService) {
    this.guiService = guiService;
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    attachEvent.getUI().access(this::createView);
  }

  private void createView() {
    this.getUI().get().setPollInterval(1000);
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
    new Label("Zone: " + zoneName);

    tabs.add(zoneName.name(), layout);
  }

  private HorizontalLayout createZoneOverview(
      final ZoneName zoneName, final ComfortModuleDto comfortDto) {

    final Tile tile = new Tile("room.svg", zoneName.name());
    final Info temperature = new Info("temperatura", "°C");
    final Info humidity = new Info("wilgotność", "%");
    final Info pressure = new Info("ciśnienie", "hPa");
    tile.getDetailsContainer().add(temperature, humidity, pressure);

    // Values
    temperature.setValue(comfortDto.getSensor().getTemperature());
    humidity.setValue(comfortDto.getSensor().getHumidity());
    pressure.setValue(comfortDto.getSensor().getPressure());
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
