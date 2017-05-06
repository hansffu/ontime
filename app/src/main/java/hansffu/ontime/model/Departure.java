package hansffu.ontime.model;

import java.util.Date;

/**
 * Created by hansffu on 07.02.17.
 */

public class Departure {
    private final LineDirectionRef lineDirectionRef;
    private final String lineNumber, destination;
    private final Date time;

    public Departure(String lineRef, String direction, String lineNumber, String destination, Date time) {
        this.lineNumber = lineNumber;
        this.destination = destination;
        this.time = time;
        this.lineDirectionRef = LineDirectionRefFactory.create(lineRef, direction);
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public String getDestination() {
        return destination;
    }

    public Date getTime() {
        return time;
    }

    public LineDirectionRef getLineDirectionRef() {
        return lineDirectionRef;
    }
}
