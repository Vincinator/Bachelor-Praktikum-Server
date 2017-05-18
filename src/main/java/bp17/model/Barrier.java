package bp17.model;


import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Vincent on 16.05.2017.
 */
@Entity
public class Barrier {

    @Id
    private int barrierId;

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

    public int getBarrierId() {
        return barrierId;
    }

    public void setBarrierId(int barrierId) {
        this.barrierId = barrierId;
    }
}
