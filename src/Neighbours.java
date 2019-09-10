import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.*;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then the method start() far below.
 * - The method updateWorld() is called periodically by a Java timer.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    // Enumeration type for the Actors
    enum Actor {
        BLUE, RED, GREEN, NONE   // NONE used for empty locations
    }

    // Enumeration type for the state of an Actor
    enum State {
        UNSATISFIED,
        SATISFIED,
        NA     // Not applicable (NA), used for NONEs
    }

    private class ActorPos {
        Actor actor;
        int i;
        int j;

        public ActorPos(Actor actor, int i, int j) {
            this.actor = actor;
            this.i = i;
            this.j = j;
        }
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    Actor[][] world;              // The world is a square matrix of Actors

    // %-distribution of RED, BLUE and NONE
    private double[] dist = {0.20, 0.2, 0.20, 0.40};

    // Number of locations (places) in world (square)
    private int nLocations = 9000;

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        final double threshold = 0.7;
        //Set actor states

        // TODO Update logical state of world
        //GATHER ALL UNSATISFIED ACTORS = [[Actor.RED,i,j], [Actor.RED,i,j], [Actor.RED,i,j]]
        //RELOCATE ALL UNSATISFIED ACTORS
        State[][] states = setStates(threshold);

        relocateUnsatisfied(states);

    }

    public State[][] setStates(double threshold) {
        State[][] states = new State[side][side];
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                states[i][j] = checkNeighbours(i,j, world[i][j], threshold);
            }
        }
        return states;
    }

    public State checkNeighbours(int i, int j, Actor actor, double threshold) {
        //Kolla om threshold% av alla neighbours är samma Actor som actor
        if (actor == Actor.NONE) {
            return State.NA;
        }
        int nSatisfied = 0;
        int nNeighbours = 0;

        if (i + 1 < side) {
            nNeighbours++;
            if (world[i + 1][j] == actor ||world[i + 1][j] == Actor.NONE) {
                nSatisfied++;
            }
        }
        if (j + 1 < side) {
            nNeighbours++;
            if (world[i][j + 1] == actor || world[i][j + 1] == Actor.NONE) {
                nSatisfied++;
            }
        }
        if (i - 1 >= 0) {
            nNeighbours++;
            if (world[i - 1][j] == actor || world[i - 1][j] == Actor.NONE) {
                nSatisfied++;
            }
        }
        if (j - 1 >= 0 ) {
            nNeighbours++;
            if (world[i][j - 1] == actor || world[i][j - 1] == Actor.NONE) {
                nSatisfied++;
            }
        }
        if (i + 1 < side && j + 1 < side) {
            nNeighbours++;
            if (world[i + 1][j + 1] == actor || world[i + 1][j + 1] == Actor.NONE) {
                nSatisfied++;
            }
        }
        if (i + 1 < side && j - 1 >= 0) {
            nNeighbours++;
            if (world[i + 1][j - 1] == actor || world[i + 1][j - 1] == Actor.NONE) {
                nSatisfied++;
            }
        }
        if (i - 1 >= 0 && j + 1 < side) {
            nNeighbours++;
            if (world[i - 1][j + 1] == actor || world[i - 1][j + 1] == Actor.NONE) {
                nSatisfied++;
            }
        }
        if (i - 1 >= 0 && j - 1 >= 0) {
            nNeighbours++;
            if (world[i - 1][j - 1] == actor || world[i - 1][j - 1] == Actor.NONE) {
                nSatisfied++;
            }
        }

        //out.println("neighbours " + nNeighbours + " nsatsfied " + nSatisfied + " = " + ((double) nSatisfied / (double) nNeighbours));
        if ((((double) nSatisfied) / ((double) nNeighbours)) >= threshold) {
            return State.SATISFIED;
        } else {
            return State.UNSATISFIED;
        }
    }

    public void relocateUnsatisfied(State[][] states) {
        //Flytta alla UNSATISFIED till random NA
        ArrayList<ActorPos> unsatisfiedActors = getUnsatisfiedActors(states);
        ArrayList<ActorPos> noneActors = getNoneActors(states);
        Random random = new Random();
        int nUnsatisfied = unsatisfiedActors.size();

        for (int i = 0; i < nUnsatisfied; i++) {
            int rand = random.nextInt(noneActors.size());
            world[noneActors.get(rand).i][noneActors.get(rand).j] = unsatisfiedActors.get(i).actor;
            world[unsatisfiedActors.get(i).i][unsatisfiedActors.get(i).j] = noneActors.get(rand).actor;
            noneActors.set(rand, new ActorPos(Actor.NONE, unsatisfiedActors.get(i).i, unsatisfiedActors.get(i).j));

        }

    }

    public ArrayList<ActorPos> getUnsatisfiedActors(State[][] states) {
        ArrayList<ActorPos> unsatisfiedActors = new ArrayList<>();
        for (int i = 0; i < states.length; i++) {
            for (int j = 0; j < states.length; j++) {
                if (states[i][j] == State.UNSATISFIED) {
                    unsatisfiedActors.add(new ActorPos(world[i][j], i, j));
                }
            }
        }
        out.println("unsatisfied " + unsatisfiedActors.size());
        return unsatisfiedActors;
    }

    public ArrayList<ActorPos> getNoneActors(State[][] states) {
        ArrayList<ActorPos> noneActors = new ArrayList<>();
        for (int i = 0; i < states.length; i++) {
            for (int j = 0; j < states.length; j++) {
                if (states[i][j] == State.NA) {
                    noneActors.add(new ActorPos(world[i][j], i, j));
                }
            }
        }
        return noneActors;
    }

    private int nRedActors = 0;
    private int nBlueActors = 0;
    private int nGreenActors = 0;
    private int nNoneActors = 0;

    int side = (int) Math.sqrt(nLocations);


    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!

        // TODO Create and populate world

        world = new Actor[side][side];
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world.length; j++) {
                addActor(i, j);
            }
        }
        out.println("red: " + nRedActors + " blue: " + nBlueActors + " none: " + nNoneActors);
        out.println("world created");

        // Should be last
        fixScreenSize(nLocations);
    }

    private void addActor(int i, int j) {
        int randActor = (int) ((Math.random() ) * dist.length);
        if (randActor == 0 && nRedActors != (nLocations * dist[0])) {
            world[i][j] = Actor.RED;
            nRedActors++;
        } else if (randActor == 1 && nBlueActors != (nLocations * dist[randActor])) {
            world[i][j] = Actor.BLUE;
            nBlueActors++;
        } else if (randActor == 2 && nGreenActors != (nLocations * dist[randActor])) {
            world[i][j] = Actor.GREEN;
            nGreenActors++;
        } else if (randActor == 3 && nNoneActors != (nLocations * dist[randActor])) {
            world[i][j] = Actor.NONE;
            nNoneActors++;
        } else {
            addActor(i,j);
        }
    }


    //---------------- Methods ----------------------------

    // Check if inside world
    boolean isValidLocation(int size, int row, int col) {
        return 0 <= row && row < size &&
                0 <= col && col < size;
    }


    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    void test() {
        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {Actor.RED, Actor.RED, Actor.NONE},
                {Actor.NONE, Actor.BLUE, Actor.NONE},
                {Actor.RED, Actor.NONE, Actor.BLUE}
        };
        double th = 0.5;   // Simple threshold used for testing

        int size = testWorld.length;
        out.println(isValidLocation(size, 0, 0));
        out.println(!isValidLocation(size, -1, 0));
        out.println(!isValidLocation(size, 0, 3));
        out.println(isValidLocation(size, 2, 2));

        // TODO More tests

        exit(0);
    }

    // Helper method for testing (NOTE: reference equality)
    <T> int count(T[] arr, T toFind) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == toFind) {
                count++;
            }
        }
        return count;
    }

    // ###########  NOTHING to do below this row, it's JavaFX stuff  ###########

    double width = 400;   // Size for window
    double height = 400;
    long previousTime = nanoTime();
    final long interval = 450000000;
    double dotSize;
    final double margin = 50;

    void fixScreenSize(int nLocations) {
        // Adjust screen window depending on nLocations
        dotSize = (width - 2 * margin) / sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > interval) {
                    updateWorld();
                    renderWorld(gc, world);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Segregation Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g, Actor[][] world) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = dotSize * col + margin;
                double y = dotSize * row + margin;

                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else if (world[row][col] == Actor.GREEN) {
                    g.setFill(Color.GREEN);
                } else {
                    g.setFill(Color.WHITE);
                }
                g.fillRect(x, y, dotSize, dotSize);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
