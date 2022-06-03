package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;

import java.util.LinkedList;
import java.util.List;

public class Physics implements ContactListener, StepListener {

    private World world;
    private boolean ballsMoving = true;

    private boolean whiteBallHitOtherBall = false;

    private List<Ball> ballsPocketed = new LinkedList<>();

    public Physics() {
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.addListener(this);
    }

    public List<Ball> getBallsPocketed() {
        List<Ball> temp = this.ballsPocketed;
        this.ballsPocketed = new LinkedList<>();
        return temp;
    }

    @Override
    public void begin(Step step, World world) {
        this.ballsMoving =  world.getBodies().stream().anyMatch(body -> !body.getLinearVelocity().isZero());
    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        if (point.isSensor()) { // => true if a pocket is in contact
            //TODO only remove ball if center of ball is in pocket
            Ball ball;
            if (point.getBody1().getUserData() instanceof Ball) {
                ball = (Ball) point.getBody1().getUserData();
            } else {
                ball = (Ball) point.getBody2().getUserData();
            }
            this.ballsPocketed.add(ball);
        }
        return true;
    }

    public World getWorld() {
        return world;
    }

    public boolean areBallsMoving() {
        return ballsMoving;
    }


    //below are only empty/default implementations
    @Override
    public void updatePerformed(Step step, World world) {

    }

    @Override
    public void postSolve(Step step, World world) {

    }

    @Override
    public void end(Step step, World world) {

    }

    @Override
    public void sensed(ContactPoint point) {

    }

    @Override
    public boolean begin(ContactPoint point) {
        Object body1 = point.getBody1().getUserData();
        Object body2 = point.getBody2().getUserData();

        if (body1 instanceof Ball && body2 instanceof Ball) {
            Ball ball1 = (Ball) body1;
            Ball ball2 = (Ball) body2;

            if (ball1.isWhite() || ball2.isWhite()) {
                this.whiteBallHitOtherBall = true;
            }
        }
        return true;
    }

    @Override
    public void end(ContactPoint point) {

    }
    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {

    }

    public boolean isWhiteBallHitOtherBall() {
        return whiteBallHitOtherBall;
    }

    public void setWhiteBallHitOtherBall(boolean whiteBallHitOtherBall) {
        this.whiteBallHitOtherBall = whiteBallHitOtherBall;
    }
}
