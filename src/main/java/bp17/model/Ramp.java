package bp17.model;

public class  Ramp extends Obstacle{

	public Ramp(){
	}

	public Ramp(String name, double longitude, double latitude){
		super(name, ObstacleTypes.RAMP, longitude, latitude);
	}
}