package net.hirschauer.yaas.lighthouse.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MidiLogEntry {
	
	private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	public static final String NOTE_ON = "Note on";
	public static final String NOTE_OFF = "Note off";
	public static final String CC = "Control change";
	public static final int STATUS_NOTE_ON = 144;
	public static final int STATUS_NOTE_OFF = 128;
	public static final int STATUS_CC = 176;
	
	private String timeString;
	private String channel;
	private int status;
	private String data1;
	private String data2;
	private String description;
	
	public MidiLogEntry() {
		long timestamp = System.currentTimeMillis();
		timeString = dateFormat.format(new Date(timestamp));
	}
	
	public String getTimeString() {
		return timeString;
	}
	public void setTimeString(String timeString) {
		this.timeString = timeString;
	}
	public String getEventType() {
		switch (status) {
			case STATUS_NOTE_OFF:
				return NOTE_OFF;
			case STATUS_CC:
				return CC;
			case STATUS_NOTE_ON:
				return NOTE_ON;
		}
		return "" + status;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getData1() {
		return data1;
	}
	public void setData1(String data1) {
		this.data1 = data1;
	}
	public String getData2() {
		return data2;
	}
	public void setData2(String data2) {
		this.data2 = data2;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
