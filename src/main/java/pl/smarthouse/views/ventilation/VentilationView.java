package pl.smarthouse.views.ventilation;

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
import pl.smarthouse.components.ValueContainer;
import pl.smarthouse.service.ParamsService;
import pl.smarthouse.service.module.ModuleService;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;
import pl.smarthouse.views.MainView;
import pl.smarthouse.views.ventilation.tabs.OverviewTab;
import pl.smarthouse.views.ventilation.tabs.ParamTab;
import pl.smarthouse.views.ventilation.tabs.ZoneTab;

@PageTitle("Smart Portal | Ventilation")
@Route(value = "Ventilation", layout = MainView.class)
@Slf4j
public class VentilationView extends VerticalLayout {
  private final ParamsService paramsService;
  private final ValueContainer valueContainer;
  private final VentModuleDto ventModuleDto;
  TabSheet tabs;

  public VentilationView(
      @Autowired final ModuleService moduleService, @Autowired final ParamsService paramsService) {

    this.paramsService = paramsService;
    ventModuleDto =
        (VentModuleDto)
            moduleService.getModuleDtos().stream()
                .filter(moduleDto -> moduleDto.getType().contains("VENTILATION"))
                .findFirst()
                .get();
    valueContainer = new ValueContainer(ventModuleDto);

    createView();
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              log.info("Pool listener triggered for class: {}", this.getClass().toString());
              valueContainer.updateValues();
            });
  }

  private void createView() {
    tabs = new TabSheet();
    add(tabs);
    tabs.add("overview", new OverviewTab(valueContainer).get());
    tabs.add("zones", new ZoneTab(valueContainer).get(ventModuleDto));

    final VentModuleParamsDto ventModuleParamsDto =
        paramsService.getParams(ventModuleDto.getServiceAddress(), VentModuleParamsDto.class);
    final VerticalLayout paramLayout = new ParamTab().get(ventModuleParamsDto);

    final Button saveButton = new Button("Save all");
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickListener(buttonClickEvent -> saveAction(ventModuleDto, ventModuleParamsDto));
    paramLayout.add(saveButton);
    tabs.add("settings", paramLayout);
  }

  private void saveAction(
      final VentModuleDto ventModuleDto, final VentModuleParamsDto ventModuleParamsDto) {

    paramsService.saveParams(
        ventModuleDto.getServiceAddress(), VentModuleParamsDto.class, ventModuleParamsDto);
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
