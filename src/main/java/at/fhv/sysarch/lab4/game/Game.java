package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.FrameListener;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

public class Game implements FrameListener {

    //TODO foul for when white ball doesn't hit anything

    //TODO universal foul method with message-parameter

    //TODO refactor to seperate the game-statemachine from the input
    private enum State {
        WAITING_FOR_INPUT,
        AIMING,
        ROLLING
    }

    private State state = State.WAITING_FOR_INPUT;

    private boolean player1Turn = false;

    private int player1Score = 0;
    private int player2Score = 0;

    private final Renderer renderer;
    private final Physics physics;
    private final Cue cue = new Cue();

    private long lastAction = System.currentTimeMillis();

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        this.initWorld();
    }
    //handling user input
    public void onMousePressed(MouseEvent e) {
        if (this.state != State.WAITING_FOR_INPUT) {
            return;
        }
        //cleanup-stuff
        this.renderer.setFoulMessage(null);
        this.renderer.setActionMessage(null);


        this.state = State.AIMING;
        this.renderer.setDrawCue(true);
        double x = e.getX();
        double y = e.getY();
        this.renderer.setCueStart(new Vector2(x, y));
        this.renderer.setCueEnd(new Vector2(x, y));

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
            // raycast throws an exception if the direction is the zero vector
            // happens if the mouse is pressed and released without dragging
            // would probably better to use the ball-center in that case
            direction = new Vector2(0.00001, 0);
        }

        Ray ray = new Ray(this.cue.getStart(), direction);
        ArrayList<RaycastResult> results = new ArrayList<>();
        this.physics.getWorld().raycast(ray, 0.1, false, false, results);

        Vector2 finalDirection = direction;
        results.stream()
                .filter(raycastResult -> raycastResult.getBody().getUserData() instanceof Ball)
                .findFirst().ifPresent(ball ->  {
            this.renderer.setStrikeMessage(null);
            this.lastAction = System.currentTimeMillis();
            ball.getBody().applyForce(finalDirection.multiply(400));

            //handling game logic e.g. if it was the white ball... could be extracted
            Ball castBall = (Ball) ball.getBody().getUserData();
            if (!castBall.isWhite()) {
                this.foul("Hit non-white-ball");
            }
        });
    }

    public void setOnMouseDragged(MouseEvent e) {
        if (this.state != State.AIMING) {
            return;
        }
        double x = e.getX();
        double y = e.getY();
        this.renderer.setCueEnd(new Vector2(x, y));

        double pX = renderer.screenToPhysicsX(x);
        double pY = renderer.screenToPhysicsY(y);

        this.cue.setEnd(new Vector2(pX, pY));
    }

    @Override
    public void onFrame(double dt) {
        if (this.state == State.WAITING_FOR_INPUT) {
            this.renderer.setStrikeMessage( "Next strike: Player " + (player1Turn ? "1" : "2"));
        }

        //check if balls are rolling
        if (this.state == State.ROLLING && !this.physics.areBallsMoving() && this.lastAction < System.currentTimeMillis() - 200) {
            //TODO extract this to some sort of turn-complete mechanism currently if a foul occurs player is switched twice
            this.turnComplete();
        }

        //handle pocketed balls
        List<Ball> pocketedBalls = physics.getBallsPocketed();
        if (pocketedBalls.isEmpty()) {
            //TODO check if a turn has been completed then switch players here
            // prolly dont need this here, check this in this::turnComplete
        }

        for (Ball b : pocketedBalls) {
            //TODO game event for pocketing white ball

            if (b.isWhite()) {
                b.setPosition(Table.Constants.WIDTH * 0.25, 0);
                b.getBody().setLinearVelocity(0, 0);

                this.foul("White ball was pocketed");

                continue;
            }

            physics.getWorld().removeBody(b.getBody());
            renderer.removeBall(b);

            this.addToScore(1);

            if (physics.getWorld().getBodyCount() <= 2) { //if only the white ball and the table remain
                physics.getWorld().removeAllBodies();
                this.initWorld();
            }
        }
    }

    //game-state mostly here
    private void turnComplete() {
        //TODO also switch players if no balls where pocketed
        this.state = State.WAITING_FOR_INPUT;

        if (fouloccured) {
            this.addToScore(-1);
            this.switchPlayers();
        }

        this.fouloccured = false;
    }

    private boolean fouloccured = false;
    private void foul(String foulMessage) {
        this.renderer.setFoulMessage(foulMessage);
        this.fouloccured = true;
    }
    private void switchPlayers() {
        this.player1Turn = !this.player1Turn;
        this.renderer.setActionMessage("Switching players");
    }

    private void addToScore(int delta) {
        if (player1Turn) {
            this.player1Score += delta;
            this.renderer.setPlayer1Score(this.player1Score);
        } else {
            this.player2Score += delta;
            this.renderer.setPlayer2Score(this.player2Score);
        }
    }


    //initialisation
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
        Ball.WHITE.getBody().setLinearVelocity(0, 0);
        physics.getWorld().addBody(Ball.WHITE.getBody());
        renderer.addBall(Ball.WHITE);
        
        Table table = new Table();
        physics.getWorld().addBody(table.getBody());
        renderer.setTable(table);
    }
}