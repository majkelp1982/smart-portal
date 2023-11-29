package pl.smarthouse.views.fireplace;

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
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleDto;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleParamsDto;
import pl.smarthouse.views.MainView;
import pl.smarthouse.views.fireplace.tabs.OverviewTab;
import pl.smarthouse.views.fireplace.tabs.ParamTab;

@PageTitle("Smart Portal | External lights")
@Route(value = "Fireplace", layout = MainView.class)
@EnableScheduling
public class FireplaceView extends VerticalLayout {
  private final ParamsService paramsService;
  private final WebService webService;
  private final ValueContainer valueContainer;
  private final FireplaceModuleDto fireplaceModuleDto;
  TabSheet tabs;

  public FireplaceView(
      @Autowired final GuiService guiService,
      @Autowired final ParamsService paramsService,
      @Autowired final WebService webService) {

    this.paramsService = paramsService;
    this.webService = webService;
    fireplaceModuleDto =
        (FireplaceModuleDto)
            guiService.getModuleDtos().stream()
                .filter(moduleDto -> moduleDto.getModuleName().contains("FIREPLACE"))
                .findFirst()
                .get();
    valueContainer = new ValueContainer(fireplaceModuleDto);

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
    tabs.add("overview", new OverviewTab(valueContainer, fireplaceModuleDto, webService).get());

    final FireplaceModuleParamsDto fireplaceModuleParamsDto =
        paramsService.getParams(
            fireplaceModuleDto.getServiceAddress(), FireplaceModuleParamsDto.class);
    final VerticalLayout paramLayout = new ParamTab().get(fireplaceModuleParamsDto);

    final Button saveButton = new Button("Save all");
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickListener(
        buttonClickEvent -> saveAction(fireplaceModuleDto, fireplaceModuleParamsDto));
    paramLayout.add(saveButton);
    tabs.add("settings", paramLayout);
  }

  private void saveAction(
      final FireplaceModuleDto fireplaceModuleDto,
      final FireplaceModuleParamsDto fireplaceModuleParamsDto) {

    paramsService.saveParams(
        fireplaceModuleDto.getServiceAddress(),
        FireplaceModuleParamsDto.class,
        fireplaceModuleParamsDto);
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
