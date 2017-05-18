package bp17.model;

/**
 * Created by Bi Banh Bao on 18.05.2017
 * this class represents Elevator on the street, which helps disable people mobilize.
 */
public class Elevator extends Obstacle {
    /**
     * attribute from and to describe the 2 streets the elevator connects
     */
    private String from;
    private String to;

    public Elevator(String name, double longitude, double latitude, String from, String to) {
        super(name, ObstacleTypes.ELEVATOR, longitude, latitude);
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
