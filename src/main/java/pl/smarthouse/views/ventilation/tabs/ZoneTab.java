package pl.smarthouse.views.ventilation.tabs;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.NativeLabel;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.views.utils.ColorPredicates;

@RequiredArgsConstructor
public class ZoneTab {
  private final ValueContainer valueContainer;

  public VerticalLayout get(final VentModuleDto ventModuleDto) {

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
              if (i.addAndGet(1) == 2) {
                horizontalTiles.add(new HorizontalLayout());
                i.set(0);
              }
            });
    horizontalTiles.forEach(horizontalTile -> zoneTab.add(horizontalTile));

    return zoneTab;
  }

  private Tile createZone(final ZoneName zoneName) {
    final NativeLabel zoneNameLabel = new NativeLabel(zoneName.toString());
    final Tile zoneTile = new Tile("place.svg", zoneNameLabel);

    final Info operation = new Info("operation");
    ColorPredicates.assignToCurrentOperation(operation);
    final Info currentPosition = new Info("position");
    ColorPredicates.assignToThrottleState(currentPosition);
    final Info goalPosition = new Info("goal");
    ColorPredicates.assignToThrottleState(goalPosition);
    final Info requiredPower = new Info("power", "%");
    ColorPredicates.assignNotZeroState(requiredPower);
    final Info lastUpdate = new Info("update");
    ColorPredicates.assignToUpdateTimestamp(lastUpdate);

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
}
