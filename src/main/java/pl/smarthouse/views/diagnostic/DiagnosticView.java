package pl.smarthouse.views.diagnostic;

import static pl.smarthouse.service.ErrorHandlingService.PORTAL_MODULE;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import pl.smarthouse.model.diagnostic.ErrorPredictionDiagnostic;
import pl.smarthouse.service.DiagnoseService;
import pl.smarthouse.service.ErrorHandlingService;
import pl.smarthouse.views.MainView;

@PageTitle("Smart Portal | Diagnostic")
@Route(value = "Diagnostic", layout = MainView.class)
@Slf4j
public class DiagnosticView extends VerticalLayout {
  private final DiagnoseService diagnoseService;
  private final ErrorHandlingService errorHandlingService;
  private final Grid<ErrorPredictionDiagnostic> errorsGrid = new Grid<>();
  private final Label totalErrorCountLabel = new Label();

  public DiagnosticView(
      @Autowired final DiagnoseService diagnoseService,
      @Autowired final ErrorHandlingService errorHandlingService) {
    this.diagnoseService = diagnoseService;
    this.errorHandlingService = errorHandlingService;
    createView();
    refreshErrors();
    errorsGrid.setItems(diagnoseService.getErrors());
    UI.getCurrent().addPollListener(pollEvent -> refreshErrors());
  }

  private void refreshErrors() {
    this.diagnoseService
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
        .doOnSubscribe(subscription -> log.info("Refreshing module errors"))
        .subscribe();
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    UI.getCurrent().setPollInterval(60000);
  }

  @Override
  protected void onDetach(final DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    UI.getCurrent().setPollInterval(-1);
  }

  private void createView() {
    final HorizontalLayout topLayout = prepareTopLayout();
    prepareGrid();
    add(topLayout, errorsGrid);
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

  private void prepareGrid() {
    errorsGrid.addColumn(ErrorPredictionDiagnostic::getModuleName).setHeader("Module");
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
  }
}
