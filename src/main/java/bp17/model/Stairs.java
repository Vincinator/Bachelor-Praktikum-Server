package bp17.model;

public class Stairs extends Obstacle{
	private int numberOfStairs;
	private boolean handleAvailable;

	public Stairs(){

	}

	public Stairs(String name, double longitude, double latitude, int numberOfStairs, boolean handleAvailable){
		super(name, ObstacleTypes.STAIRS, longitude, latitude);
		this.numberOfStairs = numberOfStairs;
		this.handleAvailable = handleAvailable;
	}

	public int getNumberOfStairs() {
        return numberOfStairs;
    }

    public void setNumberOfStairs(int num) {
        this.numberOfStairs = num;
    }

    public boolean getHandleAvailable() {
        return handleAvailable;
    }

    public void setHandleAvailable(boolean bool) {
        this.handleAvailable = bool;
    }

    // Test
    public static void main(String[] args){
        Stairs astairs = new Stairs("Hauptwache",49.874978, 8.655971, 15, true);
        System.out.println("Stairs has name:"+astairs.getName()+" and long:"+astairs.getLongitude()+" and Type:"+astairs.getTypecode());
    }

}