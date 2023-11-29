package pl.smarthouse.views.fireplace.tabs;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.RequiredArgsConstructor;
import pl.smarthouse.components.*;
import pl.smarthouse.components.tiles.Ds18b20Tile;
import pl.smarthouse.service.WebService;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleDto;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.State;
import pl.smarthouse.views.utils.ColorPredicates;

@RequiredArgsConstructor
public class OverviewTab {
  private final ValueContainer valueContainer;
  private final FireplaceModuleDto fireplaceModuleDto;
  private final WebService webService;

  public VerticalLayout get() {
    final VerticalLayout overviewTab = new VerticalLayout();

    final HorizontalLayout firstLayer = new HorizontalLayout();
    firstLayer.add(statesTile());
    firstLayer.add(Ds18b20Tile.getTile(new Label("chimney"), "chimney", valueContainer));

    final HorizontalLayout secondLayer = new HorizontalLayout();
    secondLayer.add(Ds18b20Tile.getTile(new Label("water in"), "waterIn", valueContainer));

    secondLayer.add(Ds18b20Tile.getTile(new Label("water out"), "waterOut", valueContainer));

    overviewTab.add(firstLayer, secondLayer);
    return overviewTab;
  }

  private Tile statesTile() {
    final Tile tile = new Tile("light-bulb.svg", new Label("State"));
    final Info mode = new Info("mode");
    ColorPredicates.assignToMode(mode);
    final Info pump = new Info("pump");
    ColorPredicates.assignOnOffState(pump);
    final Info throttleCurrentPosition = new Info("throttle current", "%");
    ColorPredicates.assignNotZeroState(throttleCurrentPosition);
    final Info throttleGoalPosition = new Info("throttle goal", "%");
    ColorPredicates.assignNotZeroState(throttleGoalPosition);

    final Button startButton = new Button("start");
    startButton
        .getSource()
        .addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
    ColorPredicates.assignOnOffState(startButton);
    startButton
        .getSource()
        .addClickListener(
            event -> {
              fireplaceModuleDto.setState(
                  fireplaceModuleDto.getState().equals(State.ON) ? State.OFF : State.ON);
              webService.patch(constructForceLightsUrl(), String.class, "").subscribe();
            });
    ColorPredicates.assignOnOffState(startButton);

    tile.getDetailsContainer()
        .add(
            startButton.getLayout(),
            mode.getLayout(),
            pump.getLayout(),
            throttleCurrentPosition.getLayout(),
            throttleGoalPosition.getLayout());

    valueContainer.put("state", startButton);
    valueContainer.put("mode", mode);
    valueContainer.put("pump", pump);
    valueContainer.put("throttle.currentPosition", throttleCurrentPosition);
    valueContainer.put("throttle.goalPosition", throttleGoalPosition);
    return tile;
  }

  private String constructForceLightsUrl() {
    return String.format("http://%s/state/toggle", fireplaceModuleDto.getServiceAddress());
  }
}
