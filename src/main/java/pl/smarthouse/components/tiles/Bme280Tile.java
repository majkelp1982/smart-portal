package pl.smarthouse.components.tiles;

import lombok.experimental.UtilityClass;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.NativeLabel;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.views.utils.ColorPredicates;

@UtilityClass
public class Bme280Tile {

  public Tile getTile(
      final NativeLabel labelName, final String path, final ValueContainer valueContainer) {
    final Tile tile = new Tile("thermometer.svg", labelName);

    final Info temperature = new Info("temp", "°C");
    final Info pressure = new Info("pressure", "hPa");
    final Info humidity = new Info("humidity", "%");
    final Info error = new Info("error");
    ColorPredicates.assignToError(error);
    final Info update = new Info("update");
    ColorPredicates.assignToUpdateTimestamp(update);

    tile.getDetailsContainer()
        .add(
            temperature.getLayout(),
            pressure.getLayout(),
            humidity.getLayout(),
            error.getLayout(),
            update.getLayout());

    valueContainer.put(path + ".temperature", temperature);
    valueContainer.put(path + ".pressure", pressure);
    valueContainer.put(path + ".humidity", humidity);
    valueContainer.put(path + ".!error", error);
    valueContainer.put(path + ".!responseUpdate", update);
    return tile;
  }
}
