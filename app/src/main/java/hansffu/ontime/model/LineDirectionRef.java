package hansffu.ontime.model;

/**
 * Created by hansffu on 11.02.17.
 */
public class LineDirectionRef {
    private final String lineRef;
    private final String direction;

    LineDirectionRef(String lineRef, String destinationRef) {

        this.lineRef = lineRef;
        this.direction = destinationRef;
    }

    public String getLineRef() {
        return lineRef;
    }

    public String getDestinationRef() {
        return direction;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj instanceof LineDirectionRef) {
            LineDirectionRef other = (LineDirectionRef) otherObj;
            return lineRef.equals(other.getLineRef()) && direction.equals(other.getDestinationRef());
        }
        return false;
    }
}
