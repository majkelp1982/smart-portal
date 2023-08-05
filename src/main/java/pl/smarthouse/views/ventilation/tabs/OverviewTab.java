package pl.smarthouse.views.ventilation.tabs;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.RequiredArgsConstructor;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.components.tiles.Bme280Tile;
import pl.smarthouse.components.tiles.Ds18b20Tile;

@RequiredArgsConstructor
public class OverviewTab {
  private final ValueContainer valueContainer;

  public VerticalLayout get() {
    final VerticalLayout overviewTab = new VerticalLayout();
    final HorizontalLayout fanAndActors = new HorizontalLayout();
    fanAndActors.add(createFanTile(), pumpAirConThrottle());
    overviewTab.add(fanAndActors, airExchangerLayout(), forcedAirLayout());
    return overviewTab;
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
}
