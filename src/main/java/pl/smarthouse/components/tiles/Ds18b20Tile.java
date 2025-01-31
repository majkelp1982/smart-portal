package pl.smarthouse.components.tiles;

import lombok.experimental.UtilityClass;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.NativeLabel;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.views.utils.ColorPredicates;

@UtilityClass
public class Ds18b20Tile {
  public Tile getTile(
      final NativeLabel labelName, final String path, final ValueContainer valueContainer) {
    final Tile tile = new Tile("thermometer.svg", labelName);
    final Info temperature = new Info("temp", "°C");
    final Info error = new Info("error");
    ColorPredicates.assignToError(error);
    final Info update = new Info("update");
    ColorPredicates.assignToUpdateTimestamp(update);

    tile.getDetailsContainer().add(temperature.getLayout(), error.getLayout(), update.getLayout());

    valueContainer.put(path + ".temp", temperature);
    valueContainer.put(path + ".error", error);
    valueContainer.put(path + ".lastUpdate", update);
    return tile;
  }
}
