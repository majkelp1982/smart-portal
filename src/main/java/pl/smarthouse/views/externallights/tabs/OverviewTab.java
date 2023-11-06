package pl.smarthouse.views.externallights.tabs;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.RequiredArgsConstructor;
import pl.smarthouse.components.*;
import pl.smarthouse.service.WebService;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleDto;
import pl.smarthouse.sharedobjects.dto.externallights.core.LightZoneDto;
import pl.smarthouse.views.utils.ColorPredicates;

@RequiredArgsConstructor
public class OverviewTab {
  private final ValueContainer valueContainer;
  private final ExternalLightsModuleDto externalLightsModuleDto;
  private final WebService webService;

  public VerticalLayout get() {
    final VerticalLayout overviewTab = new VerticalLayout();

    final HorizontalLayout firstLayer = new HorizontalLayout();
    firstLayer.add(lightTile("entrance", externalLightsModuleDto.getEntrance()));
    firstLayer.add(lightTile("driveway", externalLightsModuleDto.getDriveway()));

    final HorizontalLayout secondLayer = new HorizontalLayout();
    secondLayer.add(lightTile("carport", externalLightsModuleDto.getCarport()));
    secondLayer.add(lightTile("garden", externalLightsModuleDto.getGarden()));

    overviewTab.add(firstLayer, secondLayer);
    return overviewTab;
  }

  private Tile lightTile(final String name, final LightZoneDto lightZoneDto) {
    final Tile tile = new Tile("light-bulb.svg", new Label(name));
    final Info lightsInfo = new Info("power", "%");
    ColorPredicates.assignNotZeroState(lightsInfo);
    final Button forceMaxButton = new Button("forceMax");
    forceMaxButton
        .getSource()
        .addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
    ColorPredicates.assignTrueFalseState(forceMaxButton);
    forceMaxButton
        .getSource()
        .addClickListener(
            event -> {
              lightZoneDto.setForceMax(!lightZoneDto.isForceMax());
              if (lightZoneDto.isForceMax()) {
                lightZoneDto.setForceMin(false);
              }
              webService
                  .patch(constructForceLightsUrl(name, lightZoneDto), String.class, "")
                  .subscribe();
            });
    final Button forceMinButton = new Button("forceMin");
    forceMinButton
        .getSource()
        .addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
    ColorPredicates.assignTrueFalseState(forceMinButton);
    forceMinButton
        .getSource()
        .addClickListener(
            event -> {
              lightZoneDto.setForceMin(!lightZoneDto.isForceMin());
              if (lightZoneDto.isForceMin()) {
                lightZoneDto.setForceMax(false);
              }
              webService
                  .patch(constructForceLightsUrl(name, lightZoneDto), String.class, "")
                  .subscribe();
            });

    valueContainer.put(name + ".rdbDimmerResponse.power", lightsInfo);
    valueContainer.put(name + ".forceMax", forceMaxButton);
    valueContainer.put(name + ".forceMin", forceMinButton);

    tile.getDetailsContainer()
        .add(lightsInfo.getLayout(), forceMaxButton.getLayout(), forceMinButton.getLayout());
    return tile;
  }

  private String constructForceLightsUrl(final String zoneName, final LightZoneDto lightZoneDto) {
    return String.format(
        "http://%s/%s?forceMax=%s&forceMin=%s",
        externalLightsModuleDto.getServiceAddress(),
        zoneName,
        lightZoneDto.isForceMax(),
        lightZoneDto.isForceMin());
  }
}
