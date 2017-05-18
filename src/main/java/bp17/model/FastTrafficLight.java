package bp17.model;

/**
 * Created by Bi Banh Bao on 18.05.2017.
 */
public class FastTrafficLight extends Obstacle{
    /**
     * duration of the traffic light in seconds
     */
    private int duration;

    public FastTrafficLight() {
    }
    public FastTrafficLight(String name, double longitude, double latitude, int duration) {
        super(name, ObstacleTypes.FAST_TRAFFIC_LIGHT, longitude, latitude);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
