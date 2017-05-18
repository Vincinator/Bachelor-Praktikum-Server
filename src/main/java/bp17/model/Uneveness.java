package bp17.model;

public class Uneveness extends Obstacle{
	private double length;

	public Uneveness(){

	}

	public Uneveness(String name, double longitude, double latitude, double length){
		super(name, ObstacleTypes.UNEVENESS, longitude, latitude);
		this.length = length;
	}

	public double getLength() {
        return length;
    }

    public void setLength(double num) {
        this.length = num;
    }
}