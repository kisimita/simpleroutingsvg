package smartcity.bosch.com.myapplication;

/**
 *  id="block_Capitol"
     x="50"
     y="32.5"
     fill="#FFFFFF"
     stroke="#000000"
     stroke-width="2"
     stroke-miterlimit="10"
     width="101.25"
     height="124.75"
 */
public class SVGBlockInfo {
    private String id;
    private float x;
    private float y;
    private String fillColor;
    private String strokeColor;
    private int strokeWidth;
    private int strokeMiterLimit;
    private float width;
    private float height;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getStrokeMiterLimit() {
        return strokeMiterLimit;
    }

    public void setStrokeMiterLimit(int strokeMiterLimit) {
        this.strokeMiterLimit = strokeMiterLimit;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getSimpleName() {
        String[] idParts = this.id.split("_");
        return idParts[1];
    }
}
