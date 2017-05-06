package hansffu.ontime.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hansffu on 12.02.17.
 */

public class LineDirectionRefFactory {
    static List<LineDirectionRef> existing = new LinkedList<>();

    public static LineDirectionRef create(String lineRef, String direction) {
        LineDirectionRef newRef = new LineDirectionRef(lineRef, direction);
        for (LineDirectionRef oldRef : existing) {
            if (newRef.equals(oldRef)) {
                return oldRef;
            }
        }
        existing.add(newRef);
        return newRef;
    }
}
