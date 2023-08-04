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

    overviewTab.add(fanAndActors, airExchangerLayout());

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
        exchangerInletTile(),
        exchangerOutletTile(),
        exchangerFreshAirTile(),
        exchangerUsedAirTile());
    return layout;
  }

  private Tile exchangerInletTile() {
    final Tile inletTile = new Tile("thermometer.svg", "Exchanger inlet");

    final Info inletTemperature = new Info("temperature", "°C");
    final Info inletPressure = new Info("pressure", "hPa");
    final Info inletHumidity = new Info("humidity", "%");
    final Info inletError = new Info("error");
    final Info inletUpdate = new Info("update");

    inletTile
        .getDetailsContainer()
        .add(
            inletTemperature.getLayout(),
            inletPressure.getLayout(),
            inletHumidity.getLayout(),
            inletError.getLayout(),
            inletUpdate.getLayout());

    valueContainer.put("airExchanger.inlet.temperature", inletTemperature);
    valueContainer.put("airExchanger.inlet.pressure", inletPressure);
    valueContainer.put("airExchanger.inlet.humidity", inletHumidity);
    valueContainer.put("airExchanger.inlet.error", inletError);
    valueContainer.put("airExchanger.inlet.!responseUpdate", inletError);
    return inletTile;
  }

  private Tile exchangerOutletTile() {
    final Tile outletTile = new Tile("thermometer.svg", "Exchanger inlet");

    final Info outletTemperature = new Info("temperature", "°C");
    final Info outletPressure = new Info("pressure", "hPa");
    final Info outletHumidity = new Info("humidity", "%");
    final Info outletError = new Info("error");
    final Info outletUpdate = new Info("update");

    outletTile
        .getDetailsContainer()
        .add(
            outletTemperature.getLayout(),
            outletPressure.getLayout(),
            outletHumidity.getLayout(),
            outletError.getLayout(),
            outletUpdate.getLayout());

    valueContainer.put("airExchanger.outlet.temperature", outletTemperature);
    valueContainer.put("airExchanger.outlet.pressure", outletPressure);
    valueContainer.put("airExchanger.outlet.humidity", outletHumidity);
    valueContainer.put("airExchanger.outlet.error", outletError);
    valueContainer.put("airExchanger.outlet.!responseUpdate", outletError);
    return outletTile;
  }

  private Tile exchangerFreshAirTile() {
    final Tile freshAirTile = new Tile("thermometer.svg", "Exchanger fresh air");

    final Info freshAirTemperature = new Info("temperature", "°C");
    final Info freshAirPressure = new Info("pressure", "hPa");
    final Info freshAirHumidity = new Info("humidity", "%");
    final Info freshAirError = new Info("error");
    final Info freshAirUpdate = new Info("update");

    freshAirTile
        .getDetailsContainer()
        .add(
            freshAirTemperature.getLayout(),
            freshAirPressure.getLayout(),
            freshAirHumidity.getLayout(),
            freshAirError.getLayout(),
            freshAirUpdate.getLayout());

    valueContainer.put("airExchanger.freshAir.temperature", freshAirTemperature);
    valueContainer.put("airExchanger.freshAir.pressure", freshAirPressure);
    valueContainer.put("airExchanger.freshAir.humidity", freshAirHumidity);
    valueContainer.put("airExchanger.freshAir.error", freshAirError);
    valueContainer.put("airExchanger.freshAir.!responseUpdate", freshAirError);
    return freshAirTile;
  }

  private Tile exchangerUsedAirTile() {
    final Tile usedAirTile = new Tile("thermometer.svg", "Exchanger used air");

    final Info usedAirTemperature = new Info("temperature", "°C");
    final Info usedAirPressure = new Info("pressure", "hPa");
    final Info usedAirHumidity = new Info("humidity", "%");
    final Info usedAirError = new Info("error");
    final Info usedAirUpdate = new Info("update");

    usedAirTile
        .getDetailsContainer()
        .add(
            usedAirTemperature.getLayout(),
            usedAirPressure.getLayout(),
            usedAirHumidity.getLayout(),
            usedAirError.getLayout(),
            usedAirUpdate.getLayout());

    valueContainer.put("airExchanger.userAir.temperature", usedAirTemperature);
    valueContainer.put("airExchanger.userAir.pressure", usedAirPressure);
    valueContainer.put("airExchanger.userAir.humidity", usedAirHumidity);
    valueContainer.put("airExchanger.userAir.error", usedAirError);
    valueContainer.put("airExchanger.userAir.!responseUpdate", usedAirError);
    return usedAirTile;
  }

  private Tile forcedAirTile() {
    return null;
    // TODO
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
