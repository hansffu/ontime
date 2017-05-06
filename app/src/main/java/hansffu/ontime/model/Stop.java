package hansffu.ontime.model;

/**
 * Created by hansffu on 04.02.17.
 */

public class Stop {
    private final String name;
    private final long id;

    public Stop(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
