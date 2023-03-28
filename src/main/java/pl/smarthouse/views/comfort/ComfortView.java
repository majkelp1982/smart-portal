package pl.smarthouse.views.comfort;

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
import pl.smarthouse.properties.GuiComfortServiceProperties;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.views.MainView;

@PageTitle("Smart Portal | Comfort")
@Route(value = "Comfort", layout = MainView.class)
@Service
public class ComfortView extends VerticalLayout {
  private final GuiComfortServiceProperties guiComfortServiceProperties;
  TabSheet tabs;
  HorizontalLayout overviewTab;

  public ComfortView(@Autowired final GuiComfortServiceProperties guiComfortServiceProperties) {
    this.guiComfortServiceProperties = guiComfortServiceProperties;
    tabs = new TabSheet();
    tabs.add("Overview", overviewTab());
    add(tabs);
  }

  private HorizontalLayout overviewTab() {
    overviewTab = new HorizontalLayout();
    return overviewTab;
  }

  public ZoneName createZone(final ZoneName zoneName) {
    // Add zone to overview
    overviewTab.add(createZoneOverview(zoneName));

    // Create tab for zone
    final VerticalLayout layout = new VerticalLayout();
    new Label("Zone: " + zoneName);

    tabs.add(zoneName.name(), layout);
    return zoneName;
  }

  private HorizontalLayout createZoneOverview(final ZoneName zoneName) {
    final Tile tile = new Tile("room.svg", zoneName.name());
    final Info temperature = new Info("temperatura", "°C");
    final Info humidity = new Info("wilgotność", "%");
    final Info pressure = new Info("ciśnienie", "hPa");
    tile.getDetailsContainer().add(temperature, humidity, pressure);
    return tile;
  }
}
