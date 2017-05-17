package bp17.model;

/**
 * Created by Vincent on 16.05.2017.
 */
public class Barrier {

    private String name;

    private double longitude = 49.874978;
    private double latitude = 8.655971;

    public Barrier(){

    }

    public Barrier(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
