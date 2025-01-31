package pl.smarthouse.views.diagnostic;

import static pl.smarthouse.service.DiagnoseService.WAITING_FOR_MODULE_RESPONSE;
import static pl.smarthouse.service.ErrorHandlingService.PORTAL_MODULE;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import pl.smarthouse.model.ComponentColor;
import pl.smarthouse.model.diagnostic.ErrorPredictionDiagnostic;
import pl.smarthouse.model.diagnostic.ModuleDetails;
import pl.smarthouse.service.DiagnoseService;
import pl.smarthouse.service.ErrorHandlingService;
import pl.smarthouse.views.MainView;

@PageTitle("Smart Portal | Diagnostic")
@Route(value = "Diagnostic", layout = MainView.class)
@Slf4j
@EnableScheduling
@PreserveOnRefresh
public class DiagnosticView extends VerticalLayout {
  static final String TOTAL_TIME = "totalTime";
  static final String ERROR_COUNT = "errorCount";
  static final String DURATION = "duration";
  final Checkbox groupErrors = new Checkbox("group errors");
  private final DiagnoseService diagnoseService;
  private final ErrorHandlingService errorHandlingService;
  private final Grid<ErrorPredictionDiagnostic> errorsGrid = new Grid<>();
  private final Grid<ModuleDetails> moduleDetailsGrid = new Grid<>();
  private final NativeLabel totalErrorCountLabel = new NativeLabel();
  private final NativeLabel totalModuleDetailsCountLabel = new NativeLabel();
  private final Set<ModuleDetails> modulesDetails = new HashSet<>();

  public DiagnosticView(
      @Autowired final DiagnoseService diagnoseService,
      @Autowired final ErrorHandlingService errorHandlingService) {
    this.diagnoseService = diagnoseService;
    this.errorHandlingService = errorHandlingService;
    groupErrors.setValue(true);
    createView();
  }

  @Scheduled(fixedDelay = 10000)
  private void refreshModuleDetails() {
    if (!isAttached()) {
      return;
    }
    diagnoseService
        .getModulesDetails()
        .doOnNext(this::updateModulesDetails)
        .doOnNext(
            ignore ->
                getUI()
                    .ifPresent(
                        ui ->
                            ui.access(
                                () -> {
                                  moduleDetailsGrid.setItems(modulesDetails);
                                  moduleDetailsGrid.setHeightFull();
                                  moduleDetailsGrid.setAllRowsVisible(true);
                                  totalModuleDetailsCountLabel.getStyle().set("margin", "auto");
                                  if (modulesDetails.size() != diagnoseService.getModuleCount()) {
                                    totalModuleDetailsCountLabel
                                        .getStyle()
                                        .set("color", ComponentColor.ALARM.value);
                                  } else {
                                    totalModuleDetailsCountLabel
                                        .getStyle()
                                        .set("color", ComponentColor.OK.value);
                                  }
                                  totalModuleDetailsCountLabel.setText(
                                      String.format(
                                          "Modules: %s/%s",
                                          modulesDetails.size(), diagnoseService.getModuleCount()));
                                })))
        .subscribe();
  }

  private void updateModulesDetails(final ModuleDetails moduleDetails) {
    if (StringUtil.isBlank(moduleDetails.getType())) {
      return;
    }
    modulesDetails.remove(moduleDetails);
    modulesDetails.add(moduleDetails);
  }

  @Scheduled(fixedDelay = 20000)
  private void refreshModuleErrors() {
    if (!isAttached()) {
      return;
    }
    diagnoseService.updateModulesErrors().subscribe();
  }

  @Scheduled(fixedDelay = 10000)
  private void refreshErrorDetails() {
    if (!isAttached()) {
      return;
    }
    getUI()
        .ifPresent(
            ui ->
                ui.access(
                    () -> {
                      diagnoseService.updateErrors(
                          PORTAL_MODULE, errorHandlingService.getErrorPredictions());
                      final ConcurrentLinkedQueue<ErrorPredictionDiagnostic> errors =
                          diagnoseService.getErrors(groupErrors.getValue());
                      errorsGrid.setItems(errors);
                      errorsGrid.setHeightFull();
                      errorsGrid.setAllRowsVisible(true);
                      totalErrorCountLabel.getStyle().set("margin", "auto");
                      final List<String> awaitingResponseModules =
                          errors.stream()
                              .filter(
                                  errorPredictionDiagnostic ->
                                      WAITING_FOR_MODULE_RESPONSE.equals(
                                          errorPredictionDiagnostic.getMessage()))
                              .map(ErrorPredictionDiagnostic::getType)
                              .toList();
                      if (!awaitingResponseModules.isEmpty()) {
                        totalErrorCountLabel.setText(
                            String.format(
                                "Modules awaiting response: %s", awaitingResponseModules));
                        totalErrorCountLabel.getStyle().set("color", ComponentColor.ALARM.value);
                      } else if (errors.isEmpty()) {
                        totalErrorCountLabel.getStyle().set("color", ComponentColor.OK.value);
                        totalErrorCountLabel.setText("No errors found");
                      } else {
                        totalErrorCountLabel.getStyle().set("color", ComponentColor.ALARM.value);
                        totalErrorCountLabel.setText(
                            String.format("Total errors found: %s", errors.size()));
                      }
                    }));
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    UI.getCurrent().setPollInterval(5000);
    diagnoseService.initModuleErrors();
    refreshErrorDetails();
    refreshModuleDetails();
  }

  @Override
  protected void onDetach(final DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    UI.getCurrent().setPollInterval(-1);
  }

  private void createView() {
    add(createModuleDetailLayout(), createErrorLayout());
  }

  private VerticalLayout createErrorLayout() {
    final HorizontalLayout topLayout = prepareErrorTopLayout();
    prepareErrorGrid();
    return new VerticalLayout(topLayout, errorsGrid);
  }

  private HorizontalLayout prepareErrorTopLayout() {
    final HorizontalLayout layout = new HorizontalLayout();
    final Button acknowledgeAllPendingButton = new Button("Acknowledge all pending");
    acknowledgeAllPendingButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
    acknowledgeAllPendingButton.addClickListener(
        e -> {
          diagnoseService.acknowledgeAllPending();
        });

    groupErrors.addValueChangeListener(
        event -> {
          addColumnsBaseOnGroupCheckbox();
          refreshErrorDetails();
        });
    layout.add(acknowledgeAllPendingButton, groupErrors, totalErrorCountLabel);
    return layout;
  }

  private void prepareErrorGrid() {
    errorsGrid.removeAllColumns();
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getType).setKey("Module").setHeader("Module");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getMessage).setHeader("Message");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getPriority).setHeader("Prio");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getBeginTimeString).setHeader("Begin");
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getEndTimeString).setHeader("End");
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
    addColumnsBaseOnGroupCheckbox();
  }

  private void addColumnsBaseOnGroupCheckbox() {
    if (groupErrors.getValue()) {
      if (errorsGrid.getColumnByKey(DURATION) != null) {
        errorsGrid.removeColumnByKey(DURATION);
      }
      errorsGrid
          .addColumn(ErrorPredictionDiagnostic::getErrorCount)
          .setHeader("error count")
          .setSortable(true)
          .setKey(ERROR_COUNT);
      errorsGrid
          .addColumn(ErrorPredictionDiagnostic::getTotalTimeInMinutes)
          .setHeader("Total time[min]")
          .setSortable(true)
          .setKey(TOTAL_TIME);
    } else {
      if (errorsGrid.getColumnByKey(ERROR_COUNT) != null) {
        errorsGrid.removeColumnByKey(ERROR_COUNT);
      }
      if (errorsGrid.getColumnByKey(TOTAL_TIME) != null) {
        errorsGrid.removeColumnByKey(TOTAL_TIME);
      }
      errorsGrid
          .addColumn(ErrorPredictionDiagnostic::getDuration)
          .setHeader("Duration[s]")
          .setSortable(true)
          .setKey(DURATION);
    }
  }

  private VerticalLayout createModuleDetailLayout() {
    final HorizontalLayout topLayout = prepareModuleDetailTopLayout();
    prepareModuleDetailsGrid();
    return new VerticalLayout(topLayout, moduleDetailsGrid);
  }

  private HorizontalLayout prepareModuleDetailTopLayout() {
    final HorizontalLayout layout = new HorizontalLayout();
    final Button restartAllModulesButton = new Button("Restart all modules");
    restartAllModulesButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
    restartAllModulesButton.addClickListener(
        event -> {
          modulesDetails.stream()
              .map(ModuleDetails::getModuleIpAddress)
              .forEach(diagnoseService::restartModule);
        });

    layout.add(totalModuleDetailsCountLabel, restartAllModulesButton);
    return layout;
  }

  private void prepareModuleDetailsGrid() {
    moduleDetailsGrid.addColumn(ModuleDetails::getType).setKey("Module").setHeader("Module");
    moduleDetailsGrid
        .addColumn(
            new ComponentRenderer<>(
                moduleDetails -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final Button button =
                      new Button(
                          moduleDetails
                              .getServiceAddress()
                              .substring(
                                  moduleDetails.getServiceAddress().indexOf(":") + 1,
                                  moduleDetails.getServiceAddress().indexOf("/")));
                  button.addClickListener(
                      buttonClickEvent -> {
                        UI.getCurrent()
                            .getPage()
                            .executeJs(
                                "window.open('http://"
                                    + moduleDetails
                                        .getServiceAddress()
                                        .substring(
                                            0, moduleDetails.getServiceAddress().indexOf("/"))
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
    moduleDetailsGrid.addColumn(ModuleDetails::getMacAddress).setHeader("MAC address");
    moduleDetailsGrid.addColumn(ModuleDetails::getVersion).setHeader("Version");

    moduleDetailsGrid.addColumn(ModuleDetails::getFirmware).setHeader("Firmware");
    moduleDetailsGrid.addColumn(ModuleDetails::getReconnectCount).setHeader("module reconnect");
    moduleDetailsGrid.addColumn(ModuleDetails::getUptimeInMinutes).setHeader("module uptime [min]");
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
