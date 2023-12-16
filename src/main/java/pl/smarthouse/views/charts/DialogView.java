package pl.smarthouse.views.charts;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import pl.smarthouse.service.ChartService;

@RequiredArgsConstructor
public class DialogView {
  private final ChartService chartService;

  public Dialog createDialog(final Consumer<Set<String>> eventListenerConsumer) {
    final Notification notification = new Notification();
    notification.setDuration(1000);
    final Map<String, List<String>> fieldMap = new HashMap<>();
    chartService.getFieldsMapFromModules().doOnNext(fieldMap::putAll).blockLast();

    chartService.prepareMultiSelectListBox(fieldMap, eventListenerConsumer);

    final HorizontalLayout layout = createLayout(chartService.getMultiSelectListsMap());

    final Dialog manageDialog = new Dialog();
    manageDialog.setHeight("600px");
    manageDialog.setModal(true);
    manageDialog.setResizable(true);
    manageDialog.setDraggable(true);
    manageDialog.setCloseOnOutsideClick(true);
    manageDialog.setMinWidth("300px");
    manageDialog.removeAll();
    manageDialog.add(layout);

    final Button closeButton = new Button("close");
    closeButton.addClickListener(buttonClickEvent -> manageDialog.close());
    final Button deselectAllButton = new Button("deselect all");
    deselectAllButton.addClickListener(buttonClickEvent -> chartService.deselectAllItems());
    manageDialog.getFooter().add(deselectAllButton, closeButton);
    final H2 headline = new H2("Manage charts");
    final HorizontalLayout header = new HorizontalLayout(headline);
    header.getElement().getClassList().add("draggable");
    header.getStyle().set("cursor", "move");
    header.setWidthFull();
    header.setSpacing(true);
    manageDialog.getHeader().add(header);
    return manageDialog;
  }

  private HorizontalLayout createLayout(final Map<String, MultiSelectListBox> multiSelectListsMap) {
    final HorizontalLayout layout = new HorizontalLayout();
    multiSelectListsMap.keySet().stream()
        .sorted()
        .forEach(
            moduleName ->
                layout.add(moduleLayout(moduleName, multiSelectListsMap.get(moduleName))));
    return layout;
  }

  private VerticalLayout moduleLayout(
      final String moduleName, final MultiSelectListBox multiSelectListBox) {
    final VerticalLayout layout = new VerticalLayout();
    final Label moduleNameLabel = new Label(moduleName);
    moduleNameLabel.getElement().getStyle().set("fontWeight", "bold");
    layout.add(moduleNameLabel);
    layout.add(multiSelectListBox);
    return layout;
  }
}
