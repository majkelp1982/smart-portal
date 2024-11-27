package pl.smarthouse.views.lightsmqtt;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.smarthouse.service.ParamsService;
import pl.smarthouse.service.WebService;
import pl.smarthouse.service.module.ModuleCreatorType;
import pl.smarthouse.service.module.ModuleService;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightsMqttDto;
import pl.smarthouse.sharedobjects.dto.lightsmqtt.LightsMqttParamDto;
import pl.smarthouse.views.MainView;
import pl.smarthouse.views.lightsmqtt.tabs.OverviewTab;
import pl.smarthouse.views.lightsmqtt.tabs.ParamTab;

@PageTitle("Smart Portal | lights")
@Route(value = "LightsMqtt", layout = MainView.class)
@Slf4j
public class LightsMqttView extends VerticalLayout {
  private final ParamsService paramsService;
  private final LightsMqttDto lightsMqttDto;
  private final OverviewTab overviewTab;
  TabSheet tabs;

  public LightsMqttView(
      @Autowired final ModuleService moduleService,
      @Autowired final ParamsService paramsService,
      @Autowired final WebService webService) {

    this.paramsService = paramsService;
    lightsMqttDto =
        (LightsMqttDto)
            moduleService.getModuleDtos().stream()
                .filter(
                    moduleDto -> moduleDto.getType().contains(ModuleCreatorType.LIGHTS_MQTT.name()))
                .findFirst()
                .get();
    overviewTab = new OverviewTab(webService, lightsMqttDto);
    createView();
    overviewTab.refreshDetails(lightsMqttDto);
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              if (isAttached()) {
                log.info("Pool listener triggered for class: {}", this.getClass());
                overviewTab.refreshDetails(lightsMqttDto);
              }
            });
  }

  private void createView() {
    tabs = new TabSheet();
    add(tabs);
    tabs.add("overview", overviewTab.get());

    final LightsMqttParamDto lightsMqttParamDto =
        paramsService.getParams(lightsMqttDto.getServiceAddress(), LightsMqttParamDto.class);
    final VerticalLayout paramLayout = new ParamTab().get(lightsMqttParamDto);

    final Button saveButton = new Button("Save all");
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickListener(buttonClickEvent -> saveAction(lightsMqttDto, lightsMqttParamDto));
    paramLayout.add(saveButton);
    tabs.add("settings", paramLayout);
  }

  private void saveAction(
      final LightsMqttDto lightsMqttDto, final LightsMqttParamDto lightsMqttParamDto) {

    paramsService.saveParams(
        lightsMqttDto.getServiceAddress(), LightsMqttParamDto.class, lightsMqttParamDto);
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
