package pl.smarthouse.views.ventilation.tabs;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.RequiredArgsConstructor;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.NativeLabel;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.components.tiles.Bme280Tile;
import pl.smarthouse.components.tiles.Ds18b20Tile;
import pl.smarthouse.views.utils.ColorPredicates;

@RequiredArgsConstructor
public class OverviewTab {
  private final ValueContainer valueContainer;

  public VerticalLayout get() {
    final VerticalLayout overviewTab = new VerticalLayout();
    final HorizontalLayout fanAndActors = new HorizontalLayout();
    fanAndActors.add(createFanTile(), modes());
    overviewTab.add(fanAndActors, airExchangerLayout(), forcedAirLayout());
    return overviewTab;
  }

  private Tile modes() {
    final Tile tile = new Tile("recu.svg", new NativeLabel("Actors"));

    final Info circuitPump = new Info("circuit pump");
    ColorPredicates.assignOnOffState(circuitPump);
    final Info airCondition = new Info("airCondition");
    ColorPredicates.assignOnOffState(airCondition);
    final Info fireplaceAirOverpressureActive = new Info("fireplace");
    ColorPredicates.assignOnOffState(fireplaceAirOverpressureActive);
    final Info intakeCurrent = new Info("intake current");
    final Info intakeGoal = new Info("intake goal");
    tile.getDetailsContainer()
        .add(
            circuitPump.getLayout(),
            airCondition.getLayout(),
            fireplaceAirOverpressureActive.getLayout(),
            intakeCurrent.getLayout(),
            intakeGoal.getLayout());

    valueContainer.put("circuitPump", circuitPump);
    valueContainer.put("airCondition", airCondition);
    valueContainer.put("fireplaceAirOverpressureActive", fireplaceAirOverpressureActive);
    valueContainer.put("intakeThrottle.currentPosition", intakeCurrent);
    valueContainer.put("intakeThrottle.goalPosition", intakeGoal);
    return tile;
  }

  private HorizontalLayout airExchangerLayout() {
    final HorizontalLayout layout = new HorizontalLayout();

    layout.add(
        Bme280Tile.getTile(
            new NativeLabel("Exchanger inlet"), "airExchanger.inlet", valueContainer),
        Bme280Tile.getTile(
            new NativeLabel("Exchanger outlet"), "airExchanger.outlet", valueContainer),
        Bme280Tile.getTile(
            new NativeLabel("Exchanger fresh-air"), "airExchanger.freshAir", valueContainer),
        Bme280Tile.getTile(
            new NativeLabel("Exchanger used-air"), "airExchanger.userAir", valueContainer));
    return layout;
  }

  private HorizontalLayout forcedAirLayout() {
    final HorizontalLayout layout = new HorizontalLayout();

    layout.add(
        Ds18b20Tile.getTile(
            new NativeLabel("Forced water-in"),
            "forcedAirSystemExchanger.watterIn",
            valueContainer),
        Ds18b20Tile.getTile(
            new NativeLabel("Forced water-out"),
            "forcedAirSystemExchanger.watterOut",
            valueContainer),
        Ds18b20Tile.getTile(
            new NativeLabel("Forced air-in"), "forcedAirSystemExchanger.airIn", valueContainer),
        Ds18b20Tile.getTile(
            new NativeLabel("Forced air-out"), "forcedAirSystemExchanger.airOut", valueContainer));
    return layout;
  }

  private Tile createFanTile() {
    final Tile fans = new Tile("fan.svg", new NativeLabel("Fans"));

    final Info inletSpeed = new Info("inlet speed", "%");
    ColorPredicates.assignNotZeroState(inletSpeed);
    final Info inletGoal = new Info("inlet goal", "%");
    ColorPredicates.assignNotZeroState(inletGoal);
    final Info inletRevolutions = new Info("inlet rev", "[min-1]");
    ColorPredicates.assignNotZeroState(inletRevolutions);
    final Info outletSpeed = new Info("outlet speed", "%");
    ColorPredicates.assignNotZeroState(outletSpeed);
    final Info outletGoal = new Info("outlet goal", "%");
    ColorPredicates.assignNotZeroState(outletGoal);
    final Info outletRevolutions = new Info("outlet rev", "[min-1]");
    ColorPredicates.assignNotZeroState(outletRevolutions);
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
