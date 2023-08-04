package pl.smarthouse.components.tiles;

import lombok.experimental.UtilityClass;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;

@UtilityClass
public class Ds18b20Tile {
  public Tile getTile(final String name, final String path, final ValueContainer valueContainer) {
    final Tile tile = new Tile("thermometer.svg", name);
    final Info temperature = new Info("T", "°C");
    final Info error = new Info("error");
    final Info update = new Info("update");

    tile.getDetailsContainer().add(temperature.getLayout(), error.getLayout(), update.getLayout());

    valueContainer.put(path + ".temp", temperature);
    valueContainer.put(path + ".error", error);
    valueContainer.put(path + ".lastUpdate", update);
    return tile;
  }
}
