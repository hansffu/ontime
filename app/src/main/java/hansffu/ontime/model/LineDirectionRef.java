package hansffu.ontime.model;

/**
 * Created by hansffu on 11.02.17.
 */
public class LineDirectionRef {
    private final String lineRef;
    private final String direction;

    LineDirectionRef(String lineRef, String direction) {

        this.lineRef = lineRef;
        this.direction = direction;
    }

    public String getLineRef() {
        return lineRef;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj instanceof LineDirectionRef) {
            LineDirectionRef other = (LineDirectionRef) otherObj;
            return lineRef.equals(other.getLineRef()) && direction.equals(other.getDirection());
        }
        return false;
    }
}
