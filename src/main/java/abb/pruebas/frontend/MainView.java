package abb.pruebas.frontend;

import com.byteowls.jopencage.JOpenCageGeocoder;
import com.byteowls.jopencage.model.JOpenCageForwardRequest;
import com.byteowls.jopencage.model.JOpenCageLatLng;
import com.byteowls.jopencage.model.JOpenCageResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import software.xdev.vaadin.maps.leaflet.flow.LMap;
import software.xdev.vaadin.maps.leaflet.flow.data.LMarker;

@Route("/mainPage")
public class MainView extends VerticalLayout{
	
	private LMap lmap;
	private LMarker lmarker;
	
	
	public MainView() {
		
		Button backButton = new Button("Volver");
		lmap = new LMap(40.397451838704484+1, -3.62457557600193+1,17);
		lmap.setSizeFull();
		VerticalLayout v = new VerticalLayout();
		v.add(lmap);
		backButton.addClickListener(event -> 
		this.coordinates1());
		//backButton.addClickListener(event ->
		//backButton.getUI().ifPresent(ui -> ui.navigate("/login")));
		add( 
			      new H1("Estas dentro"),
			      backButton,
			      v);
	}
	
	private void coordinates1() {
		JOpenCageGeocoder jOpenCageGeocoder = new JOpenCageGeocoder("f2440833ac914d929d886ccb004468bd");
		
		JOpenCageForwardRequest request = new JOpenCageForwardRequest("Calle de las Moras, 8, 28032, Madrid");
		request.setRestrictToCountryCode("es");
		
		JOpenCageResponse response = jOpenCageGeocoder.forward(request);
		JOpenCageLatLng firstResultLatLng = response.getFirstPosition();
		System.out.println(firstResultLatLng.getLat().toString() + "," + firstResultLatLng.getLng().toString());
		this.initMapComponents(firstResultLatLng.getLat(), firstResultLatLng.getLng());
	}
	
	private void initMapComponents(Double latitude, Double longitude) {
		lmarker = new LMarker(latitude, longitude);
		lmarker.setPopup("Calle de las Moras, 8, 28032, Madrid");
		lmap.addLComponents(lmarker);
	}
	
	/*private void init() {
		private GoogleMap googleMap;
		private final String apiKey = AIzaSyAZSqMyF6hIyk--9Q8rQqBabgdFSHNPSaw;
	}*/
}
