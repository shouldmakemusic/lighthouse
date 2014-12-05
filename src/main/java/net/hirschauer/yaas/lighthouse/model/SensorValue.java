package net.hirschauer.yaas.lighthouse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorValue {
	
	private static final Logger logger = LoggerFactory.getLogger(SensorValue.class);
	
	private float x = 0;
	private float y = 0;
	private float z = 0;
	
	// Filtering constant
	private final float mAlpha = 0.8f;
	
	private float[] mGravity = new float[3];
	private float[] mAccel = new float[3];
	
	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		mGravity[2] = lowPass(z, mGravity[2]);
		mAccel[2] = highPass(z, mGravity[2]);
		this.z = z;
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
		mGravity[0] = lowPass(x, mGravity[0]);
		mAccel[0] = highPass(x, mGravity[0]);
	}
	public float getY() {
		return y;
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
		logger.debug("Argument x with class " + x.getClass());
		Float floatX = (Float) x;
		Float floatY = (Float) y;
		Float floatZ = (Float) z;
		setX(floatX.floatValue());
		setY(floatY.floatValue());
		setZ(floatZ.floatValue());		
	}
}
