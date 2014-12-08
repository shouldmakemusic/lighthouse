package net.hirschauer.yaas.lighthouse.model;

import java.util.ArrayList;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorValue {
	
	private static final Logger logger = LoggerFactory.getLogger(SensorValue.class);
	
	protected float x = 0;
	protected float y = 0;
	protected float z = 0;
	
	// Filtering constant
	private final float mAlpha = 0.8f;
	
	protected float[] mGravity = new float[3];
	protected float[] mAccel = new float[3];

	private float max = 6;
	private float min = 4;
	
	private float target_min = -10;
	private float target_max = 10;
//    def get_factor(self):
//        
//        range1 = self.target_max_value - self.target_min_value
//        #print "range1=" + str(range1)
//        range2 = self.source_max_value - self.source_min_value
//        #print "range2=" + str(range2)
//        factor = 1.0 * range1/range2
//        #print factor
//        return factor
//        
//    def get_target_value(self, source_value):
//        
//        factor = self.get_factor()
//        return (source_value * factor) + abs(factor * self.source_min_value) - abs(self.target_min_value)

	private float getFactor() {
		float range1 = target_max - target_min;
		float range2 = max - min;		
		return range1/range2;
	}
	
	private float getTargetValue(float value) {
		float factor = getFactor();
//		logger.debug("factor " + factor + ", value " + value + ", min " + min + ", target_min " + target_min);
		return (value * factor) - Math.abs(factor * min) - Math.abs(target_min);
	}
	
	public SensorValue() {
	}
	
	public SensorValue(float min, float max) {
		this.max = max;
		this.min = min;
		logger.debug("Created sensor value with min " + min + " and max " + max);
	}
	
	public float getZ() {
		return z;
	}
	
	public float getZNormalized() {
		return getTargetValue(z);
	}	

	public void setZ(float z) {
		mGravity[2] = lowPass(z, mGravity[2]);
		mAccel[2] = highPass(z, mGravity[2]);
		this.z = z;
	}
	
	public float getX() {
		return x;
	}
	
	public float getXNormalized() {
//		logger.debug("X normalized: " + getTargetValue(x));
		return getTargetValue(x);
	}
	
	public void setX(float x) {
		this.x = x;
		mGravity[0] = lowPass(x, mGravity[0]);
		mAccel[0] = highPass(x, mGravity[0]);
	}

	public float getY() {
		return y;
	}
	
	public float getYNormalized() {
		return getTargetValue(y);
	}
	
	public void setY(float y) {
		mGravity[1] = lowPass(y, mGravity[1]);
		mAccel[1] = highPass(y, mGravity[1]);
		this.y = y;
	}

	// Deemphasize transient forces
	private float lowPass(float current, float gravity) {

		return gravity * mAlpha + current * (1 - mAlpha);

	}

	// Deemphasize constant forces
	private float highPass(float current, float gravity) {

		return current - gravity;
	}
	
	public void setValues(Object x, Object y, Object z) {

		Float floatX = (Float) x * 10;
		Float floatY = (Float) y * 10;
		Float floatZ = (Float) z * 10;
		setX(floatX.floatValue());
		setY(floatY.floatValue());
		setZ(floatZ.floatValue());		
	}
	
	@Override
	public String toString() {
		return "x=" + x + ", y=" + y + ", z=" + z;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false; 
		if (!(obj instanceof SensorValue)) return false;
		SensorValue o = (SensorValue) obj;
		return x == o.x && y == o.y && z == o.z;
	}
	
	@Override
	public SensorValue clone() throws CloneNotSupportedException {
		
		SensorValue v = new SensorValue();
		v.x = x;
		v.y = y;
		v.z = z;
		v.mAccel = mAccel;
		v.mGravity = mGravity;
		v.min = min;
		v.max = max;
		return v;
	}
	
	public float getXGravity() {
		return this.mGravity[0];
	}
	
	public float getYGravity() {
		return this.mGravity[1];
	}

	public float getZGravity() {
		return this.mGravity[2];
	}
	
	public float getXAccel() {
		return this.mAccel[0];
	}

	public float getYAccel() {
		return this.mAccel[1];
	}

	public float getZAccel() {
		return this.mAccel[2];
	}

}
