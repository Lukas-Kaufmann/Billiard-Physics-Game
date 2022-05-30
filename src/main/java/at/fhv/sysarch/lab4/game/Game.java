package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

public class Game {
    private enum State {
        WAITING_FOR_INPUT,
        AIMING,
        ROLLING
    }

    private State state = State.WAITING_FOR_INPUT;

    private final Renderer renderer;
    private final Physics physics;
    private Cue cue = new Cue();

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        this.physics.setGame(this);
        this.initWorld();
    }

    public void onMousePressed(MouseEvent e) {
        if (this.state != State.WAITING_FOR_INPUT) {
            System.out.println("No-op mouseclick");
            return;
        }
        this.state = State.AIMING;
        this.renderer.setDrawCue(true);
        double x = e.getX();
        double y = e.getY();

        double pX = this.renderer.screenToPhysicsX(x);
        double pY = this.renderer.screenToPhysicsY(y);

        this.cue.setStart(new Vector2(pX, pY));
    }

    public void onMouseReleased(MouseEvent e) {
        if (this.state != State.AIMING) {
            return;
        }

        this.state = State.ROLLING;
        this.renderer.setDrawCue(false);
        double x = e.getX();
        double y = e.getY();

        double pX = this.renderer.screenToPhysicsX(x);
        double pY = this.renderer.screenToPhysicsY(y);
        this.cue.setEnd(new Vector2(pX, pY));

        Vector2 direction = new Vector2(
                this.cue.getStart().x - this.cue.getEnd().x,
                this.cue.getStart().y - this.cue.getEnd().y
        );

        if (direction.isZero()) {
            //raycasting throws an exception of the direction is the zero vector
            // happens if the mouse is pressed and released without dragging
            // would probably better to use the ball-center in that case
            direction = new Vector2(0.00001, 0);
        }

        Ray ray = new Ray(this.cue.getStart(), direction);
        ArrayList<RaycastResult> results = new ArrayList<>();
        boolean result = this.physics.getWorld().raycast(ray, 1, false, true, results);

        if(result) {
            Vector2 finalDirection = direction;
            results.stream()
                    .filter(b -> b.getBody().getUserData() instanceof Ball)
                    .findFirst()
                    .ifPresent(ball -> ball.getBody().applyForce(finalDirection.multiply(400)));
        }
    }

    public void setOnMouseDragged(MouseEvent e) {
        if (this.state != State.AIMING) {
            return;
        }
        double x = e.getX();
        double y = e.getY();

        double pX = renderer.screenToPhysicsX(x);
        double pY = renderer.screenToPhysicsY(y);

        this.cue.setEnd(new Vector2(pX, pY));
    }

    private void placeBalls(List<Ball> balls) {
        Collections.shuffle(balls);

        // positioning the billard balls IN WORLD COORDINATES: meters
        int row = 0;
        int col = 0;
        int colSize = 5;

        double y0 = -2*Ball.Constants.RADIUS*2;
        double x0 = -Table.Constants.WIDTH * 0.25 - Ball.Constants.RADIUS;

        for (Ball b : balls) {
            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);
            renderer.addBall(b);

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
        }
    }

    private void initWorld() {
        List<Ball> balls = new ArrayList<>();
        
        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;
            balls.add(b);
            physics.getWorld().addBody(b.getBody());
        }
       
        this.placeBalls(balls);

        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        physics.getWorld().addBody(Ball.WHITE.getBody());
        renderer.addBall(Ball.WHITE);

        this.renderer.setCue(this.cue);
        
        Table table = new Table();
        physics.getWorld().addBody(table.getBody());
        renderer.setTable(table);
    }

    public void ballsStopped() {
        if (this.state == State.ROLLING) {
            this.state = State.WAITING_FOR_INPUT;
        }
    }
}