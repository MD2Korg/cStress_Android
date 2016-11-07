package md2k.mcerebrum.cstress.library;

/*

The MIT License (MIT)

Copyright (c) 2013 Andreas Wolke

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

 */


import java.io.Serializable;
import java.util.Arrays;

/**
 * Reference https://github.com/jacksonicson/psquared
 */
public class PSquared implements Serializable {

    final int MARKERS = 5;

    // Percentile to find
    final double p;

    // Last percentile value
    private double pValue;

    // Initial observations
    private double[] initial = new double[MARKERS];
    private int initialCount = 0;
    private boolean initialized = false;

    // Marker heights
    private double[] q = new double[MARKERS];

    // Marker positions
    private int[] n = new int[MARKERS];

    // Desired marker positions
    private double[] n_desired = new double[MARKERS];

    // Precalculated desired marker increments
    private double[] dn = new double[MARKERS];

    // Last k value
    private int lastK;

    public PSquared(double p) {
        // Set percentile
        this.p = p;
    }

    private void init() {
        // Set initialized flag
        initialized = true;

        // Process initial observations
        for (int i = 0; i < MARKERS; i++) {
            // Set initial marker heights
            q[i] = initial[i];

            // Initial marker positions
            n[i] = i;
        }

        // Desired marker positions
        n_desired[0] = 0;
        n_desired[1] = 2 * p;
        n_desired[2] = 4 * p;
        n_desired[3] = 2 + 2 * p;
        n_desired[4] = 4;

        // Precalculated desired marker increments
        dn[0] = 0;
        dn[1] = p / 2f;
        dn[2] = p;
        dn[3] = (1f + p) / 2f;
        dn[4] = 1;
    }

    private boolean acceptInitial(double x) {
        if (initialCount < MARKERS) {
            initial[initialCount++] = x;
            Arrays.sort(initial, 0, initialCount);
            return false;
        }

        // Enough values available
        Arrays.sort(initial);
        init();
        return true;
    }

    private double initialSetPercentile() {
        int n = (int) (p * (double) initialCount);
        return initial[n];
    }

    public double accept(double x) {
        // Still recording initial values
        if (!initialized && !acceptInitial(x)) {
            pValue = initialSetPercentile();
            return pValue;
        }

        int k = -1;
        if (x < q[0]) {
            // Update minimum value
            q[0] = x;
            k = 0;
        } else if (q[0] <= x && x < q[1])
            k = 0;
        else if (q[1] <= x && x < q[2])
            k = 1;
        else if (q[2] <= x && x < q[3])
            k = 2;
        else if (q[3] <= x && x <= q[4])
            k = 3;
        else if (q[4] < x) {
            // Update maximum value
            q[4] = x;
            k = 3;
        }

        // Check if k is set properly
        assert (k >= 0);
        lastK = k;

        // Increment all positions starting at marker k+1
        for (int i = k + 1; i < MARKERS; i++)
            n[i]++;

        // Update desired marker positions
        for (int i = 0; i < MARKERS; i++)
            n_desired[i] += dn[i];

        // Adjust marker heights 2-4 if necessary
        for (int i = 1; i < MARKERS - 1; i++) {
            double d = n_desired[i] - n[i];

            if ((d >= 1 && (n[i + 1] - n[i]) > 1) || (d <= -1 && (n[i - 1] - n[i]) < -1)) {
                int ds = sign(d);

                // Try adjusting q using P-squared formula
                double tmp = parabolic(ds, i);
                if (q[i - 1] < tmp && tmp < q[i + 1]) {
                    q[i] = tmp;
                } else {
                    q[i] = linear(ds, i);
                }

                n[i] += ds;
            }

        }

        // Set current percentile value for later retrieval
        pValue = q[2];
        return q[2];
    }

    public double getPValue() {
        return pValue;
    }

    private double linear(int d, int i) {
        return q[i] + d * (q[i + d] - q[i]) / (n[i + d] - n[i]);
    }

    private double parabolic(double d, int i) {
        double a = d / (double) (n[i + 1] - n[i - 1]);

        double b = (n[i] - n[i - 1] + d) * (q[i + 1] - q[i]) / (double) (n[i + 1] - n[i])
                + (n[i + 1] - n[i] - d) * (q[i] - q[i - 1]) / (double) (n[i] - n[i - 1]);

        return q[i] + a * b;
    }

    private int sign(double d) {
        if (d >= 0)
            return 1;

        return -1;
    }

    public void dump() {
        System.out.println("initial: " + Arrays.toString(initial));
        System.out.println("k: " + lastK);
        System.out.println("q: " + Arrays.toString(q));
        System.out.println("n: " + Arrays.toString(n));
        System.out.println("n': " + Arrays.toString(n_desired));
    }
}
