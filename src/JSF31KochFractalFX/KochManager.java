package JSF31KochFractalFX;

import JSF31KochFractalFX.calculate.*;
import javafx.concurrent.Task;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Youri
 */
public class KochManager {

    public List<Edge> edges;

    private Main application;

    private final KochFractal kochFractal;
    private final KochFractal kochFractal1;
    private final KochFractal kochFractal2;

    private final CyclicBarrier cyclicBarrier;

    private List<Edge> rightEdges;
    private List<Edge> leftEdges;
    private List<Edge> bottomEdges;

    private ExecutorService pool;

    public KochManager(Main application) {
        this.application = application;

        kochFractal = new KochFractal();
        kochFractal1 = new KochFractal();
        kochFractal2 = new KochFractal();

        edges = new ArrayList<>();

        cyclicBarrier = new CyclicBarrier(3, () -> {
            System.out.println("Done with 3 threads");

            application.requestDrawEdges();

            // Shutdown the pool
            pool.shutdown();
        });
    }

    public synchronized void changeLevel(int nxt) {
        // Start the pool
        pool = Executors.newFixedThreadPool(3);

        application.clearKochPanel();

        TimeStamp ts = new TimeStamp();

        ts.setBegin("begin van de meting");

        edges.clear();

        class CalculateRightEdges extends Task<Void> implements Observer {

            private List<Edge> edges;

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof Edge) {
                    Edge edge = (Edge) arg;

                    edges.add(edge);
                }
            }

            @Override
            protected Void call() throws Exception {
                edges = new ArrayList<>();

                kochFractal.addObserver(this);
                kochFractal.setLevel(nxt);
                kochFractal.generateRightEdge();

                cyclicBarrier.await();

                rightEdges = edges;

                return null;
            }
        }

        class CalculateLeftEdges extends Task<Void> implements Observer {

            private List<Edge> edges;

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof Edge) {
                    Edge edge = (Edge) arg;

                    edges.add(edge);
                }
            }

            @Override
            protected Void call() throws Exception {
                edges = new ArrayList<>();

                kochFractal1.addObserver(this);
                kochFractal1.setLevel(nxt);
                kochFractal1.generateLeftEdge();

                cyclicBarrier.await();

                leftEdges = edges;

                return null;
            }
        }

        class CalculateBottomEdges extends Task<Void> implements Observer {

            private List<Edge> edges;

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof Edge) {
                    Edge edge = (Edge) arg;

                    edges.add(edge);
                }
            }

            @Override
            protected Void call() throws Exception {
                edges = new ArrayList<>();

                kochFractal2.addObserver(this);
                kochFractal2.setLevel(nxt);
                kochFractal2.generateBottomEdge();

                cyclicBarrier.await();

                bottomEdges = edges;

                return null;
            }
        }

        Thread right = new Thread(new CalculateRightEdges());
        Thread left = new Thread(new CalculateLeftEdges());
        Thread bottom = new Thread(new CalculateBottomEdges());

        pool.execute(right);
        pool.execute(left);
        pool.execute(bottom);

        ts.setEnd("na deel 1 van werk");

        application.setTextCalc(ts.toString());

        //drawEdges();
    }

    public synchronized void drawEdges() {
        TimeStamp ts = new TimeStamp();

        ts.setBegin("begin van de meting");

        application.clearKochPanel();

        edges.addAll(rightEdges);
        edges.addAll(leftEdges);
        edges.addAll(bottomEdges);

        application.setTextNrEdges(Integer.toString(edges.size()));

        for (Edge edge : edges) {
            application.drawEdge(edge);
        }

        ts.setEnd("na deel 1 van werk");

        application.setTextDraw(ts.toString());
    }
}
