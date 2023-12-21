package pl.smarthouse.views.diagnostic;

import static pl.smarthouse.service.ErrorHandlingService.PORTAL_MODULE;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.smarthouse.model.diagnostic.ErrorPredictionDiagnostic;
import pl.smarthouse.model.diagnostic.ModuleDetails;
import pl.smarthouse.service.DiagnoseService;
import pl.smarthouse.service.ErrorHandlingService;
import pl.smarthouse.views.MainView;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@PageTitle("Smart Portal | Diagnostic")
@Route(value = "Diagnostic", layout = MainView.class)
@Slf4j
public class DiagnosticView extends VerticalLayout {
  private final DiagnoseService diagnoseService;
  private final ErrorHandlingService errorHandlingService;
  private final Grid<ErrorPredictionDiagnostic> errorsGrid = new Grid<>();
  private final Grid<ModuleDetails> moduleDetailsGrid = new Grid<>();
  private final Label totalErrorCountLabel = new Label();
  private List<ModuleDetails> modulesDetails;

  public DiagnosticView(
      @Autowired final DiagnoseService diagnoseService,
      @Autowired final ErrorHandlingService errorHandlingService) {
    this.diagnoseService = diagnoseService;
    this.errorHandlingService = errorHandlingService;
    createView();
    refreshErrors().blockLast();
    refreshModuleDetails().block();
    errorsGrid.setItems(diagnoseService.getErrors());
    moduleDetailsGrid.setItems(modulesDetails);
    UI.getCurrent()
        .addPollListener(
            pollEvent -> {
              log.info("Pool listener triggered for class: {}", this.getClass().toString());
              refreshErrors().subscribe();
              refreshModuleDetails().subscribe();
            });
  }

  private Mono<List<ModuleDetails>> refreshModuleDetails() {
    return diagnoseService
        .getModulesDetails()
        .collectList()
        .doOnNext(moduleDetails -> modulesDetails = moduleDetails)
        .doOnNext(
            moduleDetails ->
                getUI()
                    .ifPresent(ui -> ui.access(() -> moduleDetailsGrid.setItems(moduleDetails))));
  }

  private Flux<List<ErrorPredictionDiagnostic>> refreshErrors() {
    return this.diagnoseService
        .updateModulesErrors()
        .doOnNext(
            errors -> {
              getUI()
                  .ifPresent(
                      ui ->
                          ui.access(
                              () -> {
                                diagnoseService.updateErrors(
                                    PORTAL_MODULE, errorHandlingService.getErrorPredictions());
                                errorsGrid.setItems(errors);
                                totalErrorCountLabel.setText("Total error: " + errors.size());
                              }));
            })
        .doOnSubscribe(subscription -> log.info("Refreshing module errors"));
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    UI.getCurrent().setPollInterval(15000);
  }

  @Override
  protected void onDetach(final DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    UI.getCurrent().setPollInterval(-1);
  }

  private void createView() {
    final HorizontalLayout topLayout = prepareTopLayout();
    prepareErrorGrid();
    prepareModuleDetailsGrid();
    add(topLayout, errorsGrid, moduleDetailsGrid);
  }

  private HorizontalLayout prepareTopLayout() {
    final HorizontalLayout layout = new HorizontalLayout();
    final Button acknowledgeAllPendingButton = new Button("Acknowledge all pending");
    acknowledgeAllPendingButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
    acknowledgeAllPendingButton.addClickListener(
        e -> {
          diagnoseService.acknowledgeAllPending();
        });
    layout.add(acknowledgeAllPendingButton, totalErrorCountLabel);
    return layout;
  }

  private void prepareErrorGrid() {
    errorsGrid
        .addColumn(ErrorPredictionDiagnostic::getModuleName)
        .setKey("Module")
        .setHeader("Module");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getMessage).setHeader("Message");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getPriority).setHeader("Prio");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getBeginTimeString).setHeader("Begin");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getEndTimeString).setHeader("End");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getDuration).setHeader("Duration[s]");
    errorsGrid
        .getColumns()
        .forEach(
            column -> {
              column.setResizable(true);
              column.setSortable(true);
              column.setAutoWidth(true);
            });
    errorsGrid.setPageSize(1000);
    errorsGrid.setAllRowsVisible(true);
    errorsGrid.setMultiSort(true);
    errorsGrid.recalculateColumnWidths();
    errorsGrid.sort(
        Collections.singletonList(
            new GridSortOrder<>(errorsGrid.getColumnByKey("Module"), SortDirection.ASCENDING)));
  }

  private void prepareModuleDetailsGrid() {
    moduleDetailsGrid.addColumn(ModuleDetails::getModuleType).setKey("Module").setHeader("Module");
    moduleDetailsGrid
        .addColumn(
            new ComponentRenderer<>(
                moduleDetails -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final Button button =
                      new Button(
                          "Port: "
                              + moduleDetails
                                  .getServiceAddress()
                                  .substring(moduleDetails.getServiceAddress().indexOf(":")));
                  button.addClickListener(
                      buttonClickEvent -> {
                        UI.getCurrent()
                            .getPage()
                            .executeJs(
                                "window.open('http://"
                                    + moduleDetails.getServiceAddress()
                                    + "/swagger-ui.html', '_blank');");
                      });
                  layout.add(button);
                  return layout;
                }))
        .setHeader("Swagger");
    moduleDetailsGrid
        .addColumn(
            new ComponentRenderer<>(
                moduleDetails -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final Button button = new Button(moduleDetails.getModuleIpAddress());
                  button.addClickListener(
                      buttonClickEvent -> {
                        UI.getCurrent()
                            .getPage()
                            .executeJs(
                                "window.open('http://"
                                    + moduleDetails.getModuleIpAddress()
                                    + "', '_blank');");
                      });
                  layout.add(button);
                  return layout;
                }))
        .setHeader("Module");
    moduleDetailsGrid.addColumn(ModuleDetails::getFirmware).setHeader("Firmware");
    moduleDetailsGrid.addColumn(ModuleDetails::getReconnectCount).setHeader("Reconnect");
    moduleDetailsGrid
        .addColumn(ModuleDetails::getModuleLastUpdateInSec)
        .setHeader("Module last update [s]");
    moduleDetailsGrid
        .addColumn(ModuleDetails::getServiceLastUpdateInSec)
        .setHeader("Service last update [s]");
    moduleDetailsGrid
        .getColumns()
        .forEach(
            column -> {
              column.setResizable(true);
              column.setSortable(true);
              column.setAutoWidth(true);
            });
    moduleDetailsGrid.setPageSize(1000);
    moduleDetailsGrid.setAllRowsVisible(true);
    moduleDetailsGrid.setMultiSort(true);
    moduleDetailsGrid.recalculateColumnWidths();
    moduleDetailsGrid.sort(
        Collections.singletonList(
            new GridSortOrder<>(
                moduleDetailsGrid.getColumnByKey("Module"), SortDirection.ASCENDING)));
  }
}
