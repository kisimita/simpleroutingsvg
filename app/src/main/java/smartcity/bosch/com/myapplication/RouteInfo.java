package smartcity.bosch.com.myapplication;

public class RouteInfo {
    private String id;
    private String fill;
    private String stroke;
    private float strokeOpacity;
    private int weight = 1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFill() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill = fill;
    }

    public String getStroke() {
        return stroke;
    }

    public void setStroke(String stroke) {
        this.stroke = stroke;
    }

    public float getStrokeOpacity() {
        return strokeOpacity;
    }

    public void setStrokeOpacity(float strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
