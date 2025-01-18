package pl.smarthouse.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import pl.smarthouse.views.charts.ChartsView;
import pl.smarthouse.views.chillzone.ChillZoneView;
import pl.smarthouse.views.comfort.ComfortView;
import pl.smarthouse.views.diagnostic.DiagnosticView;
import pl.smarthouse.views.fireplace.FireplaceView;
import pl.smarthouse.views.lightsmqtt.LightsMqttView;
import pl.smarthouse.views.ventilation.VentilationView;
import pl.smarthouse.views.weather.WeatherView;

@PageTitle("Main")
@Route(value = "")
public class MainView extends AppLayout {
  public MainView() {
    createHeader();
    createDrawer();
  }

  private void createHeader() {
    final HorizontalLayout header = new HorizontalLayout();
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    final H3 logo = new H3("Smart Portal");
    header.add(new DrawerToggle(), logo);
    addToNavbar(true, header);
  }

  private void createDrawer() {
    final VerticalLayout drawer = new VerticalLayout();
    final RouterLink comfortViewLink =
        createDrawerElement("comfort.svg", "Comfort", ComfortView.class);
    final RouterLink ventViewLink =
        createDrawerElement("recu.svg", "Ventilation", VentilationView.class);
    final RouterLink weatherViewLink =
        createDrawerElement("cloud.svg", "Weather", WeatherView.class);
    final RouterLink lightsMqttViewLink =
        createDrawerElement("light-bulb.svg", "Lights", LightsMqttView.class);
    final RouterLink fireplaceViewLink =
        createDrawerElement("fireplace.svg", "Fireplace", FireplaceView.class);
    final RouterLink chillZoneViewLink =
        createDrawerElement("batteryCharging.svg", "Chill", ChillZoneView.class);
    final RouterLink chartsViewLink = createDrawerElement("graph.svg", "Charts", ChartsView.class);
    final RouterLink diagnosticViewLink =
        createDrawerElement("support.svg", "Diagnostic", DiagnosticView.class);

    drawer.add(
        comfortViewLink,
        ventViewLink,
        weatherViewLink,
        lightsMqttViewLink,
        fireplaceViewLink,
        chillZoneViewLink,
        chartsViewLink,
        diagnosticViewLink);
    addToDrawer(drawer);
  }

  public RouterLink createDrawerElement(
      final String imageName,
      final String name,
      final Class<? extends Component> navigationTarget) {
    // create container for elements
    final HorizontalLayout element = new HorizontalLayout();
    element.setAlignItems(FlexComponent.Alignment.CENTER);

    // create elements
    final Image image = new Image(imageName, imageName);
    image.setHeight("50px");
    final Label label = new Label(name);

    // add to horizontal layout
    element.add(image, label);

    // create link
    final RouterLink routerLink = new RouterLink("", navigationTarget);
    routerLink.getElement().appendChild(element.getElement());

    return routerLink;
  }
}
