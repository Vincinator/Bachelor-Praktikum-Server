package bp12.model;

/**
 * Created by Vincent on 16.05.2017.
 */
public class Barrier {



    private String name;

    private double longitude = 49.874978;
    private double latitude = 8.655971;

    // must have.
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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString(){
        return new StringBuffer(" Name : ").append(this.name)
                .append(" longitude : ").append(this.longitude)
                .append(" latitude : ").append(this.latitude).toString();
    }

}
