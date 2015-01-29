// Do not edit this file for homework.
// This file is our test driver, just run main.
//
// Given a UF structure of size N, run an Erdos-Renyi experiment.
// That is, add random edges until there is a single connected component.

import java.util.Random;

public class ErdosRenyi
{
    // Run a single trial, on an existing UFI object.
    public static int trial(UFI uf, long seed)
    {
        Random gen = new Random(seed);
        int N = uf.size();
        int edges = 0;
        while (uf.count() > 1) {
            int p = gen.nextInt(N), q = gen.nextInt(N);
            uf.union(p, q);
            ++edges;
        }
        return edges;
    }
    
    // Run multiple trials and report the results.
    public static void trials(int T, int N, long seed, UFIFactory fac)
    {
        System.out.printf("Running %d Erdos-Renyi trials with N=%d%n", T, N);
        long totalEdges = 0, totalDepths = 0, beg = System.currentTimeMillis();
        for (int t=1; t<=T; ++t)
        {
            UFI uf = fac.make(N);
            assert uf.sumDepths()==0;
            int e = trial(uf, seed+t);
            totalEdges += e;
            totalDepths += uf.sumDepths();
            // System.out.printf("Trial #%d: %d edges%n", t, e);
        }
        double secs = (System.currentTimeMillis()-beg)/1000.0;
        // Report the average number of edges per trial, the average
        // depth of a node, and the average number or seconds per trial.
        System.out.printf("Averages: %.2f edges, %.3f depth, %.3f seconds%n%n",
                (double)totalEdges/T, (double)totalDepths/(N*T), secs/T);
    }
    
    // A UFIFactory is a source of UFI objects.
    interface UFIFactory { UFI make(int N); }

    public static void main(String[] args)
    {
        int T=5, N=20000, seed=17;
        System.out.println("Using QuickUnionUF ...");
        trials(T, N, seed, new UFIFactory() {
            public UFI make(int N) { return new QuickUnionUF(N); }
        });
        System.out.println("Using WeightedQuickUnionUF ...");
        trials(T, N, seed, new UFIFactory() {
            public UFI make(int N) { return new WeightedQuickUnionUF(N); }
        });
        System.out.println("Using PathCompQuickUnionUF ...");
        trials(T, N, seed, new UFIFactory() {
            public UFI make(int N) { return new PathCompQuickUnionUF(N); }
        });
        
        // Try the faster implementations again, with a larger N.
        N = 200000;
        System.out.println("Using WeightedQuickUnionUF ...");
        trials(T, N, seed, new UFIFactory() {
            public UFI make(int N) { return new WeightedQuickUnionUF(N); }
        });
        System.out.println("Using PathCompQuickUnionUF ...");
        trials(T, N, seed, new UFIFactory() {
            public UFI make(int N) { return new PathCompQuickUnionUF(N); }
        });
    }
    // Just for convenience in BlueJ, this method invokes main(...),
    // but without needing any arguments.
    public static void main() { main(null); }
}
