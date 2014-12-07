package net.hirschauer.yaas.lighthouse.visual;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCServer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.SensorValue;

public class SensorController implements ChangeListener {
	
	private Logger logger = LoggerFactory.getLogger(SensorController.class);
	
    @FXML
    private BarChart<String, Integer> barChart;

    @FXML
    private CategoryAxis xAxis;

    private ObservableList<String> sensorNames = FXCollections.observableArrayList();
    private XYChart.Series<String, Integer> lastSeries;
    private LightHouseOSCServer oscServer;
   
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Get an array with the sensor value names.
        String[] shownValue = new String[] {"x", "y", "z", "lowX", "lowY", "lowZ", "highX", "highY", "highZ"};
        // Convert it to a list and add it to our ObservableList of months.
        sensorNames.addAll(Arrays.asList(shownValue));
        xAxis.setCategories(sensorNames);
    }

    /**
     * Sets the persons to show the statistics for.
     * 
     * @param persons
     */
    public void setSensorData(SensorValue value) {
        float[] values = new float[9];
        values[0] = value.getXNormalized();
        values[1] = value.getYNormalized();
        values[2] = value.getZNormalized();

        XYChart.Series<String, Integer> series = createValueDataSeries(values);
        if (!series.equals(lastSeries)) {
            if (barChart.getData().size() > 0) {
            	barChart.getData().clear();
            }
            barChart.getData().add(series);
        }
    	lastSeries = series;
    }

    /**
     * Creates a XYChart.Data object for each month. All month data is then
     * returned as a series.
     * 
     * @param monthCounter Array with a number for each month. Must be of length 12!
     * @return
     */
    private XYChart.Series<String, Integer> createValueDataSeries(float[] values) {
    	
        XYChart.Series<String,Integer> series = new XYChart.Series<String,Integer>();

        for (int i = 0; i < values.length; i++) {
            XYChart.Data<String, Integer> data = new XYChart.Data<String,Integer>(sensorNames.get(i), Math.round(values[i]));
            series.getData().add(data);
        }

        return series;
    }

	public void setOscServer(LightHouseOSCServer oscServer) {
		this.oscServer = oscServer;
		oscServer.valueProperty().addListener(this);
		logger.debug("oscServer set and listener added");
	}

	@Override
	public void changed(ObservableValue observable, Object oldValue,
			Object newValue) {
//		logger.debug("sensor changed " + newValue);
		setSensorData((SensorValue) newValue);
	}
}
