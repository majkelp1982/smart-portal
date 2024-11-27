package pl.smarthouse.views.weather;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.Label;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.components.tiles.Bme280Tile;
import pl.smarthouse.service.module.ModuleService;
import pl.smarthouse.sharedobjects.dto.weather.WeatherModuleDto;
import pl.smarthouse.views.MainView;
import pl.smarthouse.views.utils.ColorPredicates;

@PageTitle("Smart Portal | Weather")
@Route(value = "Weather", layout = MainView.class)
@Slf4j
public class WeatherView extends VerticalLayout {
  private final ValueContainer valueContainer;
  private final WeatherModuleDto weatherModuleDto;

  public WeatherView(@Autowired final ModuleService moduleService) {
    weatherModuleDto =
        (WeatherModuleDto)
            moduleService.getModuleDtos().stream()
                .filter(moduleDto -> moduleDto.getType().contains("WEATHER"))
                .findFirst()
                .get();
    valueContainer = new ValueContainer(weatherModuleDto);

    createView();
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              if (isAttached()) {
                log.info("Pool listener triggered for class: {}", this.getClass().toString());
                valueContainer.updateValues();
              }
            });
  }

  private void createView() {
    final HorizontalLayout layout = new HorizontalLayout();
    layout.add(airPollution(), airTile());

    add(layout, lightIntenseTile());
  }

  private Tile airPollution() {
    final Tile tile = new Tile("air.svg", new Label("Pollution"));
    final Info pm025 = new Info("PM2,5", "ug/m3");
    ColorPredicates.assignToValue(pm025, 50, 150);
    final Info pm10 = new Info("PM10", "ug/m3");
    ColorPredicates.assignToValue(pm10, 50, 150);
    final Info mode = new Info("Mode");
    final Info error = new Info("error");
    ColorPredicates.assignToError(error);
    final Info update = new Info("update");
    ColorPredicates.assignToUpdateTimestamp(update, 20);

    valueContainer.put("sds011Response.pm025", pm025);
    valueContainer.put("sds011Response.pm10", pm10);
    valueContainer.put("sds011Response.mode", mode);
    valueContainer.put("sds011Response.!error", error);
    valueContainer.put("sds011Response.!responseUpdate", update);

    tile.getDetailsContainer()
        .add(
            pm025.getLayout(),
            pm10.getLayout(),
            mode.getLayout(),
            error.getLayout(),
            update.getLayout());
    return tile;
  }

  private Tile airTile() {
    return Bme280Tile.getTile(new Label("Air"), "bme280Response", valueContainer);
  }

  private Tile lightIntenseTile() {
    final Tile tile = new Tile("sun.svg", new Label("Light intense"));
    final Info lightIntense = new Info("light intense", "%");
    final Info error = new Info("error");
    ColorPredicates.assignToError(error);

    valueContainer.put("lightIntense.pinValue", lightIntense);
    valueContainer.put("lightIntense.!error", error);
    tile.getDetailsContainer().add(lightIntense.getLayout());
    return tile;
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    UI.getCurrent().setPollInterval(5000);
  }

  @Override
  protected void onDetach(final DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    UI.getCurrent().setPollInterval(-1);
  }
}
