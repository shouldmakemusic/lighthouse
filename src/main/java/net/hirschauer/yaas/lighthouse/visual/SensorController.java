package net.hirschauer.yaas.lighthouse.visual;

import java.util.Arrays;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.SensorValue;
import net.hirschauer.yaas.lighthouse.model.SensorValue.SensorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorController implements ChangeListener<SensorValue> {
	
	private Logger logger = LoggerFactory.getLogger(SensorController.class);
	
    @FXML
    private BarChart<String, Integer> barChart;

    @FXML
    private CategoryAxis xAxis;

    private ObservableList<String> sensorNames = FXCollections.observableArrayList();

	private SensorType type;
   
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Get an array with the sensor value names.
        String[] shownValue = new String[] {"x", "y", "z", "lowX", "lowY", "lowZ", "highX", "highY", "highZ", "pitch", "roll", "yaw", "accel"};
        // Convert it to a list and add it to our ObservableList of months.
        sensorNames.addAll(Arrays.asList(shownValue));
        xAxis.setCategories(sensorNames);
        XYChart.Series<String,Integer> series = new XYChart.Series<String,Integer>();
    	for (int i = 0; i < sensorNames.size(); i++) {
            XYChart.Data<String, Integer> data = new XYChart.Data<String,Integer>(sensorNames.get(i), i);
            series.getData().add(data);
        }
        barChart.getData().add(series);
        barChart.animatedProperty().set(false);
    }

    /**
     * Sets the persons to show the statistics for.
     * 
     * @param persons
     */
    public void setSensorData(SensorValue value) {
        float[] values = new float[13];
        values[0] = value.getXNormalized();
        values[1] = value.getYNormalized();
        values[2] = value.getZNormalized();
        values[3] = value.getXGravity();
        values[4] = value.getYGravity();
        values[5] = value.getZGravity();
        values[6] = value.getXAccel();
        values[7] = value.getYAccel();
        values[8] = value.getZAccel();
        values[9] = value.getPitch();
        values[10] = value.getRoll();
        values[11] = value.getYaw();
        values[12] = value.getAccel();

        ObservableList<Series<String, Integer>> series = barChart.getData();
        Series<String, Integer> serie = series.get(0);
        
		for (int i=0; i<serie.getData().size(); i++) {
			XYChart.Data<String,Integer> data = serie.getData().get(i);
            data.setYValue(Math.round(values[i]));
        }

//		logger.debug("accel: " + values[12]);
    }

	public void listenTo(LightHouseOSCServer oscServer, SensorType type) {
		oscServer.valueProperty().addListener(this);
		logger.debug("oscServer set and listener for " + type + " added");
		this.type = type;		
		barChart.getData().get(0).setName(type.toString());
    	// TODO: find a way to use different colors
//		barChart.getData().get(0).getNode().getStyleClass().add("series-" + type.toString().toLowerCase());

	}
	
	public void changed(ObservableValue<? extends SensorValue> observable,
			SensorValue oldValue, SensorValue newValue) {
		
		SensorValue value = (SensorValue) newValue;
		if (value.getType().equals(this.type)) {
			setSensorData(value);
		}
	}
}
