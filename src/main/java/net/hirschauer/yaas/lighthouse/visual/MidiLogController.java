package net.hirschauer.yaas.lighthouse.visual;

import java.util.HashMap;
import java.util.Properties;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiUnavailableException;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.model.MidiLogEntry;
import net.hirschauer.yaas.lighthouse.util.IStorable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiLogController extends Controller implements IStorable {

	Logger logger = LoggerFactory.getLogger(MidiLogController.class);

	@FXML
	private TableView<MidiLogEntry> tableMidi;
	@FXML
	private TableColumn<MidiLogEntry, String> timestampColumn;
	@FXML
	private TableColumn<MidiLogEntry, String> eventTypeColumn;
	@FXML
	private TableColumn<MidiLogEntry, String> channelColumn;
	@FXML
	private TableColumn<MidiLogEntry, String> statusColumn;
	@FXML
	private TableColumn<MidiLogEntry, String> dataColumn1;
	@FXML
	private TableColumn<MidiLogEntry, String> dataColumn2;
	@FXML
	private TableColumn<MidiLogEntry, String> descriptionColumn;
	@FXML
	private ComboBox<String> midiInputCombobox;
	@FXML
	private Button btnClear;

	private HashMap<String, Info> midiInfos;
	private String name;

	@FXML
	private void initialize() {
		timestampColumn
				.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>(
						"timeString"));
		eventTypeColumn
				.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>(
						"eventType"));
		channelColumn
				.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>(
						"channel"));
		statusColumn
				.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>(
						"status"));
		dataColumn1
				.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>(
						"data1"));
		dataColumn2
				.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>(
						"data2"));
		descriptionColumn
				.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>(
						"description"));
		initMidi();
	}

	@Override
	public void store(Properties values) {
		if (name != null) {
			values.put(getClass().getName() + "|" + "midi", name);
		}
	}

	@Override
	public void load(Properties values) {
		String name = values.getProperty(getClass().getName() + "|" + "midi");
		if (StringUtils.isNoneEmpty(name)) {
			// init(name);
			midiInputCombobox.setValue(name);
		}
	}

	public void initMidi() {

		LightHouseMidi midi = LightHouseMidi.getInstance();
		tableMidi.setItems(midi.logEntries);

		btnClear.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				midi.logEntries.clear();
			}
		});

		ObservableList<String> midiNames = FXCollections.observableArrayList();
		midiInfos = midi.getPossibleMidiInfos();
		for (Info info : midiInfos.values()) {
			midiNames.add(info.getName() + " - " + info.getDescription());
		}
		midiInputCombobox.setItems(midiNames);
		midiInputCombobox.valueProperty().addListener(
			new ChangeListener<String>() {

				@Override
				public void changed(
						ObservableValue<? extends String> observable,
						String oldValue, String newValue) {

					logger.debug("selected " + newValue);
					name = newValue;
					try {
						midi.setDevice(name);
					} catch (MidiUnavailableException e) {
						logger.error("Midi not available", e);
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText("Midi is not available");
						alert.show();
					}
				}
			});
	}

	@Override
	protected String getMenuId() {
		return "midiController";
	}

}
