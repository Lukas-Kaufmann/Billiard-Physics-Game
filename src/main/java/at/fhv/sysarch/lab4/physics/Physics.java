package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Game;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;

public class Physics implements ContactListener, StepListener {

    private World world;
    private Game game;

    public Physics() {
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.addListener(this);
    }

    @Override
    public void begin(Step step, World world) {
        boolean ballsRolling = world.getBodies().stream().anyMatch(body -> !body.getLinearVelocity().isZero());

        if (!ballsRolling) {
            this.game.ballsStopped();
        }
    }

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
        return true;
    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        return true;
    }

    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {

    }

    public World getWorld() {
        return world;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
