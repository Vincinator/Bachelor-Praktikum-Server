package bp17.model;

import java.util.Date;

/**
 * This class represents the Obstacle Construction and is used to stored
 */
public class  Construction extends Obstacle{
	/**
	 * attribute size describe the size in m2 of a construction
	 */
	private double size;
	private Date validUntil;

	public Construction(){
	}

	public Construction(String name, double longitude, double latitude, double size, Date date) {
		super(name, ObstacleTypes.CONSTRUCTION, longitude, latitude);
		this.size = size;
		this.validUntil = date;
	}

	public double getSize() {
		return size;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}
}