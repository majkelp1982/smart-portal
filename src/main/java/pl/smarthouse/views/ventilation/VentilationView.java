package pl.smarthouse.views.ventilation;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import pl.smarthouse.service.GuiService;
import pl.smarthouse.views.MainView;

@PageTitle("Smart Portal | Ventilation")
@Route(value = "Ventilation", layout = MainView.class)
@EnableScheduling
public class VentilationView extends VerticalLayout {
  private final GuiService guiService;
  LocalDateTime localDateTime = LocalDateTime.now();
  Label label;
  UI ui;

  public VentilationView(@Autowired final GuiService guiService) {
    ui = UI.getCurrent();
    ui.setPollInterval(100);
    this.guiService = guiService;
    label = new Label();
    ui.addPollListener(
        pollEvent -> {
          label.setText("Time:" + localDateTime);
        });

    add(label);
  }

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    ui.setPollInterval(100);
  }

  @Override
  protected void onDetach(final DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    ui.setPollInterval(-1);
  }

  @Scheduled(fixedDelay = 100)
  private void refreshTimeStamp() {
    localDateTime = LocalDateTime.now();
    //    ui.access(() -> label.setText(LocalDateTime.now().toString()));
  }
}
