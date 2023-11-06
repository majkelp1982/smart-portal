package pl.smarthouse.views.externallights;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.service.GuiService;
import pl.smarthouse.service.ParamsService;
import pl.smarthouse.service.WebService;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleDto;
import pl.smarthouse.sharedobjects.dto.externallights.ExternalLightsModuleParamsDto;
import pl.smarthouse.views.MainView;
import pl.smarthouse.views.externallights.tabs.OverviewTab;
import pl.smarthouse.views.externallights.tabs.ParamTab;

@PageTitle("Smart Portal | External lights")
@Route(value = "ExternalLights", layout = MainView.class)
@EnableScheduling
public class ExternalLightsView extends VerticalLayout {
  private final ParamsService paramsService;
  private final WebService webService;
  private final ValueContainer valueContainer;
  private final ExternalLightsModuleDto externalLightsModuleDto;
  TabSheet tabs;

  public ExternalLightsView(
      @Autowired final GuiService guiService,
      @Autowired final ParamsService paramsService,
      @Autowired final WebService webService) {

    this.paramsService = paramsService;
    this.webService = webService;
    externalLightsModuleDto =
        (ExternalLightsModuleDto)
            guiService.getModuleDtos().stream()
                .filter(moduleDto -> moduleDto.getModuleName().contains("EXTERNAL_LIGHTS"))
                .findFirst()
                .get();
    valueContainer = new ValueContainer(externalLightsModuleDto);

    createView();
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              valueContainer.updateValues();
            });
  }

  private void createView() {
    tabs = new TabSheet();
    add(tabs);
    tabs.add(
        "overview", new OverviewTab(valueContainer, externalLightsModuleDto, webService).get());

    final ExternalLightsModuleParamsDto externalLightsModuleParamsDto =
        paramsService.getParams(
            externalLightsModuleDto.getServiceAddress(), ExternalLightsModuleParamsDto.class);
    final VerticalLayout paramLayout = new ParamTab().get(externalLightsModuleParamsDto);

    final Button saveButton = new Button("Save all");
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickListener(
        buttonClickEvent -> saveAction(externalLightsModuleDto, externalLightsModuleParamsDto));
    paramLayout.add(saveButton);
    tabs.add("settings", paramLayout);
  }

  private void saveAction(
      final ExternalLightsModuleDto externalLightsModuleDto,
      final ExternalLightsModuleParamsDto externalLightsModuleParamsDto) {

    paramsService.saveParams(
        externalLightsModuleDto.getServiceAddress(),
        ExternalLightsModuleParamsDto.class,
        externalLightsModuleParamsDto);
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
}
