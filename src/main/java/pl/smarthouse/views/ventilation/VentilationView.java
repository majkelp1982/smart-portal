package pl.smarthouse.views.ventilation;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.components.tiles.Bme280Tile;
import pl.smarthouse.components.tiles.Ds18b20Tile;
import pl.smarthouse.service.GuiService;
import pl.smarthouse.service.ParamsService;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.views.MainView;

@PageTitle("Smart Portal | Ventilation")
@Route(value = "Ventilation", layout = MainView.class)
@EnableScheduling
public class VentilationView extends VerticalLayout {
  private final GuiService guiService;
  private final ParamsService paramsService;
  private final ValueContainer valueContainer;
  private final VentModuleDto ventModuleDto;
  TabSheet tabs;

  public VentilationView(
      @Autowired final GuiService guiService, @Autowired final ParamsService paramsService) {

    this.guiService = guiService;
    this.paramsService = paramsService;
    ventModuleDto =
        (VentModuleDto)
            guiService.getModuleDtos().stream()
                .filter(moduleDto -> moduleDto.getModuleName().contains("VENTILATION"))
                .findFirst()
                .get();
    valueContainer = new ValueContainer(ventModuleDto);

    createView();
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              valueContainer.updateValues();
            });
  }

  private void createView() {
    tabs = new TabSheet();
    add(tabs);
    overviewTab();
    zoneTab();
    paramsTab();
  }

  private void overviewTab() {
    final VerticalLayout overviewTab = new VerticalLayout();

    final HorizontalLayout fanAndActors = new HorizontalLayout();
    fanAndActors.add(createFanTile(), pumpAirConThrottle());

    overviewTab.add(fanAndActors, airExchangerLayout(), forcedAirLayout());

    tabs.add("overview", overviewTab);
  }

  private Tile pumpAirConThrottle() {
    final Tile tile = new Tile("recu.svg", "Actors");

    final Info circuitPump = new Info("circuit pump");
    final Info airCondition = new Info("airCondition");
    final Info intakeCurrent = new Info("intake current");
    final Info intakeGoal = new Info("intake goal");
    tile.getDetailsContainer()
        .add(
            circuitPump.getLayout(),
            airCondition.getLayout(),
            intakeCurrent.getLayout(),
            intakeGoal.getLayout());

    valueContainer.put("circuitPump", circuitPump);
    valueContainer.put("airCondition", airCondition);
    valueContainer.put("intakeThrottle.currentPosition", intakeCurrent);
    valueContainer.put("intakeThrottle.goalPosition", intakeGoal);
    return tile;
  }

  private HorizontalLayout airExchangerLayout() {
    final HorizontalLayout layout = new HorizontalLayout();

    layout.add(
        Bme280Tile.getTile("Exchanger inlet", "airExchanger.inlet", valueContainer),
        Bme280Tile.getTile("Exchanger outlet", "airExchanger.outlet", valueContainer),
        Bme280Tile.getTile("Exchanger fresh-air", "airExchanger.freshAir", valueContainer),
        Bme280Tile.getTile("Exchanger used-air", "airExchanger.userAir", valueContainer));
    return layout;
  }

  private HorizontalLayout forcedAirLayout() {
    final HorizontalLayout layout = new HorizontalLayout();

    layout.add(
        Ds18b20Tile.getTile("Forced water-in", "forcedAirSystemExchanger.watterIn", valueContainer),
        Ds18b20Tile.getTile(
            "Forced water-out", "forcedAirSystemExchanger.watterOut", valueContainer),
        Ds18b20Tile.getTile("Forced air-in", "forcedAirSystemExchanger.airIn", valueContainer),
        Ds18b20Tile.getTile("Forced air-out", "forcedAirSystemExchanger.airOut", valueContainer));
    return layout;
  }

  private Tile createFanTile() {
    final Tile fans = new Tile("fan.svg", "Fans");

    final Info inletSpeed = new Info("inlet speed", "%");
    final Info inletGoal = new Info("inlet goal", "%");
    final Info inletRevolutions = new Info("inlet rev", "[min-1]");
    final Info outletSpeed = new Info("outlet speed", "%");
    final Info outletGoal = new Info("outlet goal", "%");
    final Info outletRevolutions = new Info("outlet rev", "[min-1]");
    fans.getDetailsContainer()
        .add(
            inletSpeed.getLayout(),
            inletGoal.getLayout(),
            inletRevolutions.getLayout(),
            outletSpeed.getLayout(),
            outletGoal.getLayout(),
            outletRevolutions.getLayout());

    valueContainer.put("fans.inlet.currentSpeed", inletSpeed);
    valueContainer.put("fans.inlet.goalSpeed", inletGoal);
    valueContainer.put("fans.inlet.revolution", inletRevolutions);

    valueContainer.put("fans.outlet.currentSpeed", outletSpeed);
    valueContainer.put("fans.outlet.goalSpeed", outletGoal);
    valueContainer.put("fans.outlet.revolution", outletRevolutions);
    return fans;
  }

  private void zoneTab() {

    final VerticalLayout zoneTab = new VerticalLayout();

    final List<HorizontalLayout> horizontalTiles = new ArrayList<>();
    horizontalTiles.add(new HorizontalLayout());
    final AtomicInteger i = new AtomicInteger(0);
    ventModuleDto.getZoneDtoHashMap().keySet().stream()
        .sorted(Comparator.comparing(Enum::toString))
        .map(this::createZone)
        .forEach(
            tile -> {
              horizontalTiles.get(horizontalTiles.size() - 1).add(tile);
              if (i.addAndGet(1) == 3) {
                horizontalTiles.add(new HorizontalLayout());
                i.set(0);
              }
            });
    horizontalTiles.forEach(horizontalTile -> zoneTab.add(horizontalTile));
    tabs.add("zones", zoneTab);
  }

  private Tile createZone(final ZoneName zoneName) {
    final Tile zoneTile = new Tile("place.svg", zoneName.toString());

    final Info operation = new Info("operation");
    final Info currentPosition = new Info("position");
    final Info goalPosition = new Info("goal");
    final Info requiredPower = new Info("power", "%");
    final Info lastUpdate = new Info("update");

    valueContainer.put("zoneDtoHashMap[" + zoneName + "].operation", operation);
    valueContainer.put(
        "zoneDtoHashMap[" + zoneName + "].throttle.currentPosition", currentPosition);
    valueContainer.put("zoneDtoHashMap[" + zoneName + "].throttle.goalPosition", goalPosition);
    valueContainer.put("zoneDtoHashMap[" + zoneName + "].requiredPower", requiredPower);
    valueContainer.put("zoneDtoHashMap[" + zoneName + "].lastUpdate", lastUpdate);

    zoneTile
        .getDetailsContainer()
        .add(
            operation.getLayout(),
            currentPosition.getLayout(),
            goalPosition.getLayout(),
            requiredPower.getLayout(),
            lastUpdate.getLayout());
    return zoneTile;
  }

  private void paramsTab() {
    final HorizontalLayout paramsTab = new HorizontalLayout();
    tabs.add("settings", paramsTab);
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    UI.getCurrent().setPollInterval(1000);
  }

  @Override
  protected void onDetach(final DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    UI.getCurrent().setPollInterval(-1);
  }
}
