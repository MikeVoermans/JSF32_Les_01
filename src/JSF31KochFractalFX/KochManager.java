package JSF31KochFractalFX;

import JSF31KochFractalFX.calculate.*;
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

    private Future<List<Edge>> rightEdges;
    private Future<List<Edge>> leftEdges;
    private Future<List<Edge>> bottomEdges;

    private ExecutorService pool;

    public KochManager(Main application) {
        this.application = application;

        kochFractal = new KochFractal();
        kochFractal1 = new KochFractal();
        kochFractal2 = new KochFractal();

        edges = new ArrayList<>();

        cyclicBarrier = new CyclicBarrier(3, () -> {
            System.out.println("Done with the 3 threads.");

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

        // Right
        class Right implements Observer, Callable<List<Edge>> {
            private List<Edge> edges;

            @Override
            public List<Edge> call() throws Exception {
                edges = new ArrayList<>();

                kochFractal.addObserver(this);
                kochFractal.setLevel(nxt);
                kochFractal.generateRightEdge();

                cyclicBarrier.await();

                return edges;
            }

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof Edge) {
                    Edge edge = (Edge) arg;

                    edges.add(edge);
                }
            }
        }

        // Left
        class Left implements Observer, Callable<List<Edge>> {
            private List<Edge> edges;

            @Override
            public List<Edge> call() throws Exception {
                edges = new ArrayList<>();

                kochFractal1.addObserver(this);
                kochFractal1.setLevel(nxt);
                kochFractal1.generateLeftEdge();

                cyclicBarrier.await();

                return edges;
            }

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof Edge) {
                    Edge edge = (Edge) arg;

                    edges.add(edge);
                }
            }
        }

        // Bottom
        class Bottom implements Observer, Callable<List<Edge>> {
            private List<Edge> edges;

            @Override
            public List<Edge> call() throws Exception {
                edges = new ArrayList<>();

                kochFractal2.addObserver(this);
                kochFractal2.setLevel(nxt);
                kochFractal2.generateBottomEdge();

                cyclicBarrier.await();

                return edges;
            }

            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof Edge) {
                    Edge edge = (Edge) arg;

                    edges.add(edge);
                }
            }
        }

        rightEdges = pool.submit(new Right());
        leftEdges = pool.submit(new Left());
        bottomEdges = pool.submit(new Bottom());

        ts.setEnd("na deel 1 van werk");

        application.setTextCalc(ts.toString());

        //drawEdges();
    }

    public synchronized void drawEdges() {
        TimeStamp ts = new TimeStamp();

        ts.setBegin("begin van de meting");

        application.clearKochPanel();

        try {
            edges.addAll(rightEdges.get());
            edges.addAll(leftEdges.get());
            edges.addAll(bottomEdges.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        application.setTextNrEdges(Integer.toString(edges.size()));

        for (Edge edge : edges) {
            application.drawEdge(edge);
        }

        ts.setEnd("na deel 1 van werk");

        application.setTextDraw(ts.toString());
    }
}
