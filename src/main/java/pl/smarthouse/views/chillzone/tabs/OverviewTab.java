package pl.smarthouse.views.chillzone.tabs;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.RequiredArgsConstructor;
import pl.smarthouse.components.*;
import pl.smarthouse.service.WebService;
import pl.smarthouse.sharedobjects.dto.chillzone.ChillZoneModuleDto;
import pl.smarthouse.sharedobjects.dto.chillzone.ChillZoneParamModuleDto;
import pl.smarthouse.sharedobjects.dto.chillzone.SpaDevice;
import pl.smarthouse.sharedobjects.dto.chillzone.SpaDeviceParam;
import pl.smarthouse.sharedobjects.dto.core.enums.State;
import pl.smarthouse.views.utils.ColorPredicates;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OverviewTab extends VerticalLayout {
  private final ValueContainer valueContainer;
  private final ChillZoneModuleDto chillZoneModuleDto;
  private final ChillZoneParamModuleDto chillZoneModuleParamsDto;
  private final WebService webService;

  public VerticalLayout get() {
    Tile sauna =
        createSpaDeviceOverview(
            "Sauna", "sauna", chillZoneModuleDto.getSauna(), chillZoneModuleParamsDto.getSauna());
    Tile chillRoom =
        createSpaDeviceOverview(
            "Chill room",
            "chillRoom",
            chillZoneModuleDto.getChillRoom(),
            chillZoneModuleParamsDto.getChillRoom());
    add(sauna, chillRoom);
    return this;
  }

  private Tile createSpaDeviceOverview(
      final String spaDeviceName,
      final String valueContainerName,
      final SpaDevice spaDevice,
      final SpaDeviceParam spaDeviceParam) {
    final Label spaDeviceNameLabel = new Label(spaDeviceName);
    ColorPredicates.assignToError(spaDeviceNameLabel);
    final Tile tile = new Tile(valueContainerName + ".svg", spaDeviceNameLabel);

    final Button stateButton = new Button(spaDevice.getState().toString());
    stateButton
        .getSource()
        .addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
    stateButton
        .getSource()
        .addClickListener(
            event -> {
              spaDevice.setState(spaDevice.getState().equals(State.ON) ? State.OFF : State.ON);
              stateButton.setValue(spaDevice.getState().toString());
              webService
                  .patch(constructSpaDeviceTriggerUrl(valueContainerName), String.class, "")
                  .onErrorResume(
                      throwable -> {
                        Notification notification = new Notification(throwable.getMessage());
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        notification.open();
                        return Mono.empty();
                      })
                  .subscribe();
            });
    ColorPredicates.assignOnOffState(stateButton);

    final Info relayState = new Info("Relay");
    ColorPredicates.assignToSpaDeviceRelayState(relayState);
    final Info temperature = new Info("temp", "°C");

    if (State.ON.equals(spaDevice.getState())) {
      temperature.setExpectedValue(spaDeviceParam.getRequiredTemperature());
    } else {
      temperature.setExpectedValue(spaDeviceParam.getMinRequiredTemperature());
    }
    ColorPredicates.assignToTemperature(temperature, -1.0d, 0.5d, 1.0d);
    final Info pressure = new Info("pressure", "hPa");
    final Info humidity = new Info("humidity", "%");
    final Info error = new Info("error");
    ColorPredicates.assignToError(error);
    final Info update = new Info("update");
    ColorPredicates.assignToUpdateTimestamp(update);
    tile.getDetailsContainer()
        .add(
            stateButton.getLayout(),
            relayState.getLayout(),
            temperature.getLayout(),
            pressure.getLayout(),
            humidity.getLayout(),
            error.getLayout(),
            update.getLayout());

    // Values
    valueContainer.put(valueContainerName + ".relayState", relayState);
    valueContainer.put(valueContainerName + ".bme280ResponseDto.temperature", temperature);
    valueContainer.put(valueContainerName + ".bme280ResponseDto.pressure", pressure);
    valueContainer.put(valueContainerName + ".bme280ResponseDto.humidity", humidity);
    valueContainer.put(valueContainerName + ".bme280ResponseDto.!error", error);
    valueContainer.put(valueContainerName + ".bme280ResponseDto.!responseUpdate", update);
    return tile;
  }

  private String constructSpaDeviceTriggerUrl(String valueContainerName) {
    return String.format(
        "http://%s/%s/state/toggle",
        chillZoneModuleDto.getServiceAddress(), valueContainerName.toLowerCase());
  }
}
