package pl.smarthouse.views.charts;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import pl.smarthouse.service.ChartService;
import pl.smarthouse.views.MainView;
import reactor.core.publisher.Flux;

@PageTitle("Smart Portal | Charts")
@Route(value = "Charts", layout = MainView.class)
public class ChartsView extends VerticalLayout {
  private final ChartService chartService;
  private final Dialog manageDialog = new Dialog();
  ApexCharts apexCharts;
  private HashMap<String, MultiSelectListBox> multiSelectListsMap = new HashMap<>();
  Consumer<Set<String>> multiSelectListMapListener = values -> updateSeries();

  public ChartsView(@Autowired final ChartService chartService) {
    this.chartService = chartService;
    createView();
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
    apexCharts = getChart();
    apexCharts.setWidth("2000px");
    apexCharts.setHeight("1000px");

    final Button manageButton = new Button("Manage charts");
    manageButton.addClickListener(
        buttonClickEvent -> {
          createDialog();
          manageDialog.open();
        });

    add(apexCharts, manageButton);
  }

  private void createDialog() {
    final VerticalLayout layout = new VerticalLayout();
    multiSelectListsMap =
        chartService.prepareMultiSelectListBox(
            chartService.getFieldsMap(), multiSelectListMapListener);
    final Accordion accordion = createAccordion(multiSelectListsMap);

    manageDialog.setMinWidth("300px");
    manageDialog.removeAll();
    manageDialog.add(accordion);
  }

  private Accordion createAccordion(final HashMap<String, MultiSelectListBox> multiSelectListsMap) {
    final Accordion accordion = new Accordion();
    multiSelectListsMap.keySet().stream()
        .sorted()
        .forEach(moduleName -> accordion.add(moduleName, multiSelectListsMap.get(moduleName)));
    return accordion;
  }

  private ApexCharts getChart() {
    return ApexChartsBuilder.get()
        .withChart(
            ChartBuilder.get()
                .withType(Type.LINE)
                .withZoom(ZoomBuilder.get().withEnabled(true).build())
                .build())
        .withStroke(StrokeBuilder.get().withCurve(Curve.STRAIGHT).withWidth(1.0).build())
        .withGrid(
            GridBuilder.get()
                .withRow(
                    RowBuilder.get().withColors("#f3f3f3", "transparent").withOpacity(0.5).build())
                .build())
        .withXaxis(
            XAxisBuilder.get()
                .withType(XAxisType.DATETIME)
                .withTooltip(TooltipBuilder.get().withEnabled(false).build())
                .build())
        .build();
  }

  private void updateSeries() {
    final HashMap<String, Set<String>> selectedItemsMap = new HashMap<>();
    multiSelectListsMap.keySet().stream()
        .filter(moduleName -> !multiSelectListsMap.get(moduleName).getSelectedItems().isEmpty())
        .forEach(
            moduleName ->
                selectedItemsMap.put(
                    moduleName, multiSelectListsMap.get(moduleName).getSelectedItems()));

    Flux.fromIterable(selectedItemsMap.keySet())
        .flatMap(
            moduleName ->
                Flux.fromIterable(selectedItemsMap.get(moduleName))
                    .flatMap(
                        item ->
                            chartService
                                .getCoordinates(
                                    moduleName,
                                    item,
                                    LocalDateTime.now().minusHours(2),
                                    LocalDateTime.now())
                                .collectList()
                                .map(
                                    coordinates -> {
                                      final Coordinate[] coordinatesArray =
                                          new Coordinate[coordinates.size()];
                                      return coordinates.toArray(coordinatesArray);
                                    })
                                .map(
                                    coordinates ->
                                        new Series<>(moduleName + "." + item, coordinates))))
        .collectList()
        .map(
            series -> {
              final Series[] seriesArray = new Series[series.size()];
              return series.toArray(seriesArray);
            })
        .doOnNext(seriesArray -> setSeries(seriesArray))
        .subscribe();
  }

  private void setSeries(final Series[] series) {
    getUI().ifPresentOrElse(ui -> ui.access(() -> apexCharts.updateSeries(series)), null);
  }
}