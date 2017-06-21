package bp17.model;


import org.eclipse.persistence.annotations.PrimaryKey;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by Bi on 18.05.2017.
 */
@Entity
public class Obstacle  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String typename;
    public ObstacleTypes typecode;

    public double longitude = 49.874978;
    public double latitude = 8.655971;

    public Obstacle(){

    }

    public Obstacle(String name, ObstacleTypes typecode, double longitude, double latitude){
        this.typename = name;
        this.typecode = typecode;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return typename;
    }

    public void setName(String name) {
        this.typename = name;
    }

    public ObstacleTypes getTypecode() {
        return typecode;
    }

    public void setTypecode(ObstacleTypes code) {
        this.typecode = code;
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


}
