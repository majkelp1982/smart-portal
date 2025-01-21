package pl.smarthouse.views.chillzone;

import static pl.smarthouse.service.module.ModuleCreatorType.CHILL_ZONE;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.service.ParamsService;
import pl.smarthouse.service.WebService;
import pl.smarthouse.service.module.ModuleService;
import pl.smarthouse.sharedobjects.dto.chillzone.ChillZoneModuleDto;
import pl.smarthouse.sharedobjects.dto.chillzone.ChillZoneParamModuleDto;
import pl.smarthouse.views.MainView;
import pl.smarthouse.views.chillzone.tabs.OverviewTab;
import pl.smarthouse.views.chillzone.tabs.ParamTab;

@PageTitle("Smart Portal | Chill zone")
@Route(value = "chillzone", layout = MainView.class)
@Slf4j
public class ChillZoneView extends VerticalLayout {
  private final ParamsService paramsService;
  private final WebService webService;
  private final ValueContainer valueContainer;
  private final ChillZoneModuleDto chillZoneModuleDto;
  private OverviewTab overviewTab;
  TabSheet tabs;

  public ChillZoneView(
      @Autowired final ModuleService moduleService,
      @Autowired final ParamsService paramsService,
      @Autowired final WebService webService) {

    this.paramsService = paramsService;
    this.webService = webService;
    chillZoneModuleDto =
        (ChillZoneModuleDto)
            moduleService.getModuleDtos().stream()
                .filter(moduleDto -> moduleDto.getType().contains(CHILL_ZONE.name()))
                .findFirst()
                .get();
    valueContainer = new ValueContainer(chillZoneModuleDto);

    createView();
    valueContainer.updateValues();
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              if (overviewTab != null) {
                overviewTab.handleNotification();
              }
              if (isAttached()) {
                log.info("Pool listener triggered for class: {}", this.getClass());
                valueContainer.updateValues();
              }
            });
  }

  private void createView() {
    tabs = new TabSheet();
    add(tabs);
    final ChillZoneParamModuleDto chillZoneModuleParamsDto =
        paramsService.getParams(
            chillZoneModuleDto.getServiceAddress(), ChillZoneParamModuleDto.class);
    overviewTab =
        new OverviewTab(valueContainer, chillZoneModuleDto, chillZoneModuleParamsDto, webService);
    tabs.add("overview", overviewTab.get());

    final VerticalLayout paramLayout = new ParamTab().get(chillZoneModuleParamsDto);

    final Button saveButton = new Button("Save all");
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickListener(
        buttonClickEvent -> saveAction(chillZoneModuleDto, chillZoneModuleParamsDto));
    paramLayout.add(saveButton);
    tabs.add("settings", paramLayout);
  }

  private void saveAction(
      final ChillZoneModuleDto chillZoneModuleDto,
      final ChillZoneParamModuleDto chillZoneModuleParamsDto) {

    paramsService.saveParams(
        chillZoneModuleDto.getServiceAddress(),
        ChillZoneParamModuleDto.class,
        chillZoneModuleParamsDto);
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
