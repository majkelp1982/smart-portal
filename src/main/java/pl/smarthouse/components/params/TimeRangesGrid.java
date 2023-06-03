package pl.smarthouse.components.params;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NonNull;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRange;

public class TimeRangesGrid extends VerticalLayout {
  private final Set<TimeRange> timeRanges;
  private final Grid<TimeRange> grid = new Grid<>(TimeRange.class, false);

  public TimeRangesGrid(final String gridName, @NonNull final Set<TimeRange> timeRanges) {
    this.timeRanges = timeRanges;

    final H5 label = new H5(gridName);
    final HorizontalLayout buttonLayout = new HorizontalLayout();

    final Icon deleteIcon = VaadinIcon.CLOSE_SMALL.create();
    deleteIcon.getElement().getThemeList().add("badge error");
    deleteIcon.getStyle().set("padding", "var(--lumo-space-xs");
    deleteIcon.addClickListener(iconClickEvent -> deleteListener());

    final Icon createIcon = VaadinIcon.PLUS.create();
    createIcon.getElement().getThemeList().add("badge success");
    createIcon.getStyle().set("padding", "var(--lumo-space-xs");
    createIcon.addClickListener(iconClickEvent -> createListener());

    buttonLayout.add(deleteIcon, createIcon);

    grid.setAllRowsVisible(true);
    grid.setSelectionMode(Grid.SelectionMode.MULTI);
    grid.addColumn(TimeRange::getFrom).setHeader("od").setAutoWidth(true).setFlexGrow(0);
    grid.addColumn(TimeRange::getTo).setHeader("do").setAutoWidth(true).setFlexGrow(0);

    add(label, grid, buttonLayout);
  }

  private Button createAddButton(final Dialog dialog, final TimePicker from, final TimePicker to) {
    final Button addButton = new Button("Add", e -> addButtonListener(dialog, from, to));
    addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    return addButton;
  }

  private void addButtonListener(final Dialog dialog, final TimePicker from, final TimePicker to) {
    if (from.getValue().isAfter(to.getValue())) {
      final Notification notification = new Notification();
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      final Div text = new Div(new Text("Invalid time range"));
      notification.add(text);
      notification.setPosition(Notification.Position.MIDDLE);
      notification.setDuration(5000);
      notification.open();
      return;
    }
    timeRanges.add(TimeRange.builder().from(from.getValue()).to(to.getValue()).build());
    setItems(timeRanges);
    dialog.close();
  }

  public void setItems(final Set<TimeRange> timeRanges) {
    if (Objects.isNull(timeRanges)) {
      return;
    }
    grid.setItems(
        timeRanges.stream()
            .sorted(Comparator.comparing(TimeRange::getFrom))
            .collect(Collectors.toList()));
  }

  private void deleteListener() {
    timeRanges.removeAll(grid.getSelectedItems());
    grid.setItems(timeRanges);
  }

  private void createListener() {
    // Dialog
    final Dialog newTimeRangeDialog = new Dialog();
    newTimeRangeDialog.setHeaderTitle("New time range");

    // Form formLayout
    final HorizontalLayout formLayout = new HorizontalLayout();
    final TimePicker timePickerFrom = new TimePicker();
    timePickerFrom.setLocale(Locale.ITALIAN);
    timePickerFrom.setLabel("From");
    timePickerFrom.setStep(Duration.ofMinutes(15));
    timePickerFrom.setValue(LocalTime.of(7, 0));

    final TimePicker timePickerTo = new TimePicker();
    timePickerTo.setLabel("To");
    timePickerTo.setStep(Duration.ofMinutes(15));
    timePickerTo.setValue(LocalTime.of(7, 10));
    formLayout.add(timePickerFrom, timePickerTo);
    newTimeRangeDialog.add(formLayout);

    final Button saveButton = createAddButton(newTimeRangeDialog, timePickerFrom, timePickerTo);
    final Button cancelButton = new Button("Cancel", e -> newTimeRangeDialog.close());
    newTimeRangeDialog.getFooter().add(cancelButton);
    newTimeRangeDialog.getFooter().add(saveButton);

    newTimeRangeDialog.open();
  }
}
