package net.hirschauer.yaas.lighthouse.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MidiLogEntry {
	
	private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private String timeString;
	private String eventType;
	private String channel;
	private String status;
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
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
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
