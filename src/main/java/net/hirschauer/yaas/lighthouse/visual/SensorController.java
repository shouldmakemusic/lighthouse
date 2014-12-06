package net.hirschauer.yaas.lighthouse.visual;

import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import net.hirschauer.yaas.lighthouse.model.SensorValue;

public class SensorController {
	
    @FXML
    private BarChart<String, Integer> barChart;

    @FXML
    private CategoryAxis xAxis;

    private ObservableList<String> sensorNames = FXCollections.observableArrayList();
   
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
        values[0] = value.getX();
        values[1] = value.getY();
        values[2] = value.getZ();

        XYChart.Series<String, Integer> series = createValueDataSeries(values);
        barChart.getData().add(series);
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
}
