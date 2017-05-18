package bp17.model;

/**
 * Created by Bi Banh Bao on 18.05.2017.
 * this class represents tight passgage through which disable people with wheelchair
 * are not able to pass.
 */
public class TightPassage extends Obstacle {
    /**
     * the width of the passage
     */
    private double width;
    private boolean smallerThan90cm;

    public TightPassage() {
    }

    public TightPassage(String name, double longitude, double latitude, double width, boolean smallerThan90cm) {
        super(name, ObstacleTypes.TIGHT_PASSAGE, longitude, latitude);
        this.width = width;
        this.smallerThan90cm = smallerThan90cm;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public boolean isSmallerThan90cm() {
        return smallerThan90cm;
    }

    public void setSmallerThan90cm(boolean smallerThan90cm) {
        this.smallerThan90cm = smallerThan90cm;
    }
}
