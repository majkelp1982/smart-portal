package pl.smarthouse.views.comfort;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.smarthouse.components.Info;
import pl.smarthouse.components.Label;
import pl.smarthouse.components.Tile;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.service.ParamsService;
import pl.smarthouse.service.module.ModuleService;
import pl.smarthouse.sharedobjects.dto.ModuleDto;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleDto;
import pl.smarthouse.sharedobjects.dto.comfort.ComfortModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.FunctionType;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.views.MainView;
import pl.smarthouse.views.comfort.subview.AirExchangerView;
import pl.smarthouse.views.comfort.subview.HumidityAlertView;
import pl.smarthouse.views.comfort.subview.TemperatureControlView;
import pl.smarthouse.views.utils.ColorPredicates;

@PageTitle("Smart Portal | Comfort")
@Route(value = "Comfort", layout = MainView.class)
@Slf4j
public class ComfortView extends VerticalLayout {
  final List<HorizontalLayout> horizontalOverviewTiles = new ArrayList<>();
  private final ModuleService moduleService;
  private final ParamsService paramsService;
  private final HashMap<String, ValueContainer> valueContainerMap = new HashMap<>();
  private final Map<String, ComfortModuleParamsDto> comfortModuleParamsDto = new HashMap<>();
  TabSheet tabs;
  VerticalLayout overviewTab = new VerticalLayout();

  public ComfortView(
      @Autowired final ModuleService moduleService, @Autowired final ParamsService paramsService) {
    this.moduleService = moduleService;
    this.paramsService = paramsService;
    createView();
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              log.info("Pool listener triggered for class: {}", this.getClass().toString());
              valueContainerMap.values().stream().forEach(ValueContainer::updateValues);
            });
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    UI.getCurrent().setPollInterval(1000);
  }

  @Override
  protected void onDetach(final DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    UI.getCurrent().setPollInterval(-1);
  }

  private void createView() {
    tabs = new TabSheet();
    tabs.add("Overview", overviewTab);
    add(tabs);

    moduleService.getModuleDtos().stream()
        .filter(moduleDto -> moduleDto.getModuleName().contains("COMFORT"))
        .sorted(Comparator.comparing(ModuleDto::getModuleName))
        .map(moduleDto -> (ComfortModuleDto) moduleDto)
        .forEach(this::createZoneTab);

    horizontalOverviewTiles.forEach(
        horizontalOverviewTile -> overviewTab.add(horizontalOverviewTile));
  }

  private void createZoneTab(final ComfortModuleDto moduleDto) {

    final ZoneName zoneName =
        ZoneName.valueOf(cutNameIfNecessaryAndReturn(moduleDto.getModuleName()));

    // Add zone to overview
    if (horizontalOverviewTiles.size() == 0) {
      horizontalOverviewTiles.add(new HorizontalLayout());
    }
    HorizontalLayout layout = horizontalOverviewTiles.get(horizontalOverviewTiles.size() - 1);
    if (layout.getComponentCount() == 2) {
      layout = new HorizontalLayout();
      horizontalOverviewTiles.add(layout);
    }
    // Create tab for zone
    tabs.add(zoneName.name(), createZoneTab(zoneName.name(), moduleDto));

    layout.add(createZoneOverview(zoneName.name(), zoneName.name(), moduleDto));
  }

  private Tile createZoneOverview(
      final String zoneName, final String valueContainerName, final ComfortModuleDto comfortDto) {
    final Label zoneNameLabel = new Label(zoneName);
    ColorPredicates.assignToError(zoneNameLabel);
    final Tile tile = new Tile("room.svg", zoneNameLabel);
    final Info temperature = new Info("temperature", "°C");
    temperature.setExpectedValue(
        comfortModuleParamsDto
            .get(comfortDto.getServiceAddress())
            .getTemperatureControl()
            .getRequiredTemperature());
    ColorPredicates.assignToTemperature(temperature, -0.5, 0.2, 0.3);
    final Info humidity = new Info("humidity", "%");
    ColorPredicates.assignToHumidity(humidity);
    final Info currentOperation = new Info("operation");
    ColorPredicates.assignToCurrentOperation(currentOperation);
    final Info requiredPower = new Info("power", "%");
    ColorPredicates.assignToRequiredPower(requiredPower);
    tile.getDetailsContainer()
        .add(
            temperature.getLayout(),
            humidity.getLayout(),
            currentOperation.getLayout(),
            requiredPower.getLayout());

    // Values
    final ValueContainer valueContainer = new ValueContainer(comfortDto);
    valueContainer.put("!error", zoneNameLabel);
    valueContainer.put("sensorResponse.temperature", temperature);
    valueContainer.put("sensorResponse.humidity", humidity);
    valueContainer.put("currentOperation", currentOperation);
    valueContainer.put("requiredPower", requiredPower);

    valueContainerMap.put(valueContainerName, valueContainer);

    return tile;
  }

  private VerticalLayout createZoneTab(final String zoneName, final ComfortModuleDto comfortDto) {
    final VerticalLayout layout = new VerticalLayout();
    final VerticalLayout paramLayout = createParamsView(comfortDto);
    final String zoneTabName = zoneName + "Tab";

    final Tile overviewTile = createZoneOverview(zoneName, zoneTabName, comfortDto);

    layout.add(enrichZoneOverviewWithDetails(zoneTabName, overviewTile), paramLayout);
    return layout;
  }

  private VerticalLayout createParamsView(final ComfortModuleDto comfortDto) {
    final VerticalLayout paramsLayout = new VerticalLayout();
    comfortModuleParamsDto.put(
        comfortDto.getServiceAddress(),
        paramsService.getParams(comfortDto.getServiceAddress(), ComfortModuleParamsDto.class));
    final Accordion accordion = new Accordion();
    AirExchangerView.addForm(
        accordion, comfortModuleParamsDto.get(comfortDto.getServiceAddress()).getAirExchanger());
    final FunctionType functionType = comfortDto.getFunctionType();
    TemperatureControlView.addForm(
        accordion,
        functionType,
        comfortModuleParamsDto.get(comfortDto.getServiceAddress()).getTemperatureControl());
    HumidityAlertView.addForm(
        accordion,
        functionType,
        comfortModuleParamsDto.get(comfortDto.getServiceAddress()).getHumidityAlert());

    final Button saveButton = new Button("Save all");
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickListener(buttonClickEvent -> saveAction(comfortDto));

    accordion.close();
    paramsLayout.add(accordion, saveButton);

    return paramsLayout;
  }

  private void saveAction(final ComfortModuleDto comfortDto) {

    paramsService.saveParams(
        comfortDto.getServiceAddress(),
        ComfortModuleParamsDto.class,
        comfortModuleParamsDto.get(comfortDto.getServiceAddress()));
  }

  private HorizontalLayout enrichZoneOverviewWithDetails(
      final String valueContainerName, final Tile zoneTabTile) {
    final VerticalLayout detailsContainer = zoneTabTile.getDetailsContainer();

    final Info leftHoldTimeInMinutes = new Info("extra hold", "min");
    final Info sensorResponseUpdateTimestamp = new Info("update");
    ColorPredicates.assignToUpdateTimestamp(sensorResponseUpdateTimestamp);

    final Info sensorError = new Info("sensor error");
    ColorPredicates.assignToError(sensorError);
    detailsContainer.add(
        leftHoldTimeInMinutes.getLayout(),
        sensorResponseUpdateTimestamp.getLayout(),
        sensorError.getLayout());

    // Values
    final ValueContainer valueContainer = valueContainerMap.get(valueContainerName);
    valueContainer.put("leftHoldTimeInMinutes", leftHoldTimeInMinutes);
    valueContainer.put("sensorResponse.!responseUpdate", sensorResponseUpdateTimestamp);
    valueContainer.put("sensorResponse.!error", sensorError);

    return zoneTabTile;
  }

  private String cutNameIfNecessaryAndReturn(final String fullComfortName) {

    if (fullComfortName.contains("COMFORT")) {
      return fullComfortName.substring(8);
    } else {
      return fullComfortName;
    }
  }
}
