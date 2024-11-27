package pl.smarthouse.views.charts;

import com.fasterxml.jackson.databind.node.TextNode;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.YAxis;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.tooltip.builder.XBuilder;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.yaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.github.appreciated.apexcharts.helper.Series;
import com.github.appreciated.apexcharts.helper.StringFormatter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
  private final HorizontalLayout apexChartHolder = new HorizontalLayout();
  private LocalDateTime chartToTimestamp = LocalDateTime.now();
  private LocalDateTime chartFromTimestamp = chartToTimestamp.minusDays(1);
  private final Consumer<Set<String>> multiSelectListMapListener = values -> updateSeries();

  public ChartsView(@Autowired final ChartService chartService) {
    this.chartService = chartService;
    createView();
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

  private void createView() {

    final HorizontalLayout bottomLayout = new HorizontalLayout();

    final Button manageButton = new Button("Manage charts");
    manageButton.addClickListener(
        buttonClickEvent -> {
          final Dialog manageDialog =
              new DialogView(chartService).createDialog(multiSelectListMapListener);
          manageDialog.open();
        });

    final DatePicker.DatePickerI18n singleFormatI18n = new DatePicker.DatePickerI18n();
    singleFormatI18n.setDateFormat("yyyy-MM-dd");
    final DatePicker datePickerFromTimestamp = new DatePicker("Show charts until:");
    datePickerFromTimestamp.setI18n(singleFormatI18n);
    datePickerFromTimestamp.setValue(LocalDate.now());
    datePickerFromTimestamp.setMax(LocalDate.now());
    datePickerFromTimestamp.addValueChangeListener(
        event -> {
          chartToTimestamp = event.getValue().atTime(LocalTime.now());
          updateSeries();
        });

    final IntegerField integerFieldMinusDays = new IntegerField("minus days");
    integerFieldMinusDays.setMin(1);
    integerFieldMinusDays.setMax(5);
    integerFieldMinusDays.setStep(1);
    integerFieldMinusDays.setStepButtonsVisible(true);
    integerFieldMinusDays.setValue(1);
    integerFieldMinusDays.addValueChangeListener(
        event -> {
          chartFromTimestamp = chartToTimestamp.minusDays(event.getValue());
          updateSeries();
        });

    bottomLayout.add(manageButton, integerFieldMinusDays, datePickerFromTimestamp);
    bottomLayout.setAlignItems(Alignment.BASELINE);

    add(apexChartHolder, bottomLayout);
    buildChart(List.of(new Series<>()));
    updateSeries();
  }

  private ApexChartsBuilder getChartBuilder() {
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
        .withTooltip(
            TooltipBuilder.get()
                .withEnabled(true)
                .withX(XBuilder.get().withFormat("dd/MM HH:mm:ss").withShow(true).build())
                .withShared(false)
                .withFillSeriesColor(false)
                .withFollowCursor(true)
                .build());
  }

  private void updateSeries() {
    Flux.fromIterable(chartService.getSelectedItems(true).keySet())
        .flatMap(
            moduleName ->
                Flux.fromIterable(chartService.getSelectedItems(false).get(moduleName))
                    .flatMap(
                        item ->
                            chartService
                                .getCoordinates(
                                    moduleName, item, chartFromTimestamp, chartToTimestamp)
                                .collectList()
                                .map(chartService::handleNotANumberValues)
                                .map(
                                    coordinates -> {
                                      coordinates.forEach(
                                          coordinate -> {
                                            if (coordinate.getY()[0] instanceof TextNode) {
                                              final String text =
                                                  ((TextNode) coordinate.getY()[0]).asText();
                                              ((TextNode) coordinate.getY()[0])
                                                  .asText(text.replace("\"", ""));
                                            }
                                          });
                                      return coordinates;
                                    })
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
        .doOnNext(this::buildChart)
        .subscribe();
  }

  private void buildChart(final List<Series<Coordinate>> series) {
    getUI()
        .ifPresent(
            ui ->
                ui.access(
                    () -> {
                      final ApexCharts chart = getChartBuilder().build();
                      final Series[] seriesArray = new Series[series.size()];
                      series.toArray(seriesArray);

                      chart.setWidth("2000px");
                      chart.setHeight("1000px");
                      chart.updateSeries(new Series());
                      chart.updateSeries(seriesArray);
                      apexChartHolder.removeAll();
                      apexChartHolder.add(chart);
                    }));
  }

  private YAxis buildYAxis(final Series<Coordinate> series) {
    if (series.getName().contains("error")) {
      return YAxisBuilder.get()
          .withLabels(
              LabelsBuilder.get()
                  .withFormatter("function(val) {if (val) {return 'true'; } else return 'false';}")
                  .build())
          .build();
    }
    if (series.getName().contains("Operation")) {
      return YAxisBuilder.get()
          .withSeriesName(series.getName())
          .withLabels(LabelsBuilder.get().withFormatter(new StringFormatter()).build())
          .build();
    }

    return YAxisBuilder.get().build();
  }
}
