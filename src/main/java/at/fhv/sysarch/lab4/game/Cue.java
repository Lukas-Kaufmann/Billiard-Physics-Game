package at.fhv.sysarch.lab4.game;

import org.dyn4j.geometry.Vector2;

public class Cue {

    private Vector2 start;
    private Vector2 end;

    public Cue() {
        this(new Vector2(0, 0), new Vector2(0, 0));
    }
    public Cue(Vector2 start, Vector2 end) {
        this.start = start;
        this.end = end;
    }

    public Vector2 getStart() {
        return start;
    }

    public void setStart(Vector2 start) {
        this.start = start;
    }

    public Vector2 getEnd() {
        return end;
    }

    public void setEnd(Vector2 end) {
        this.end = end;
    }
}
