package edu.ucsb.cs.capstone.letmypeoplecode.smartrover.test;

import android.graphics.Point;
import android.test.InstrumentationTestCase;

import edu.ucsb.cs.capstone.letmypeoplecode.smartrover.Beacon;
import edu.ucsb.cs.capstone.letmypeoplecode.smartrover.BeaconError;
import edu.ucsb.cs.capstone.letmypeoplecode.smartrover.BeaconManager;
import edu.ucsb.cs.capstone.letmypeoplecode.smartrover.Point3D;

public class TrilaterationTest extends InstrumentationTestCase {
    //The most basic test
    //4 beacons with almost no noise
    public void testSimple() throws BeaconError {
        BeaconManager mngr = new BeaconManager();
        //Arrange beacons in 4 corners of a square, all at the same height
        //Give them a preset distance
        //Rover is at (.877, .665, 0)
        double[] expected = {.877, .665, 0};

        //This format makes it easier to paste from Maple or MATLAB
        Point3D result = trilatRunHelper(
                new double[]{1, 0, 1, 0},      //x coordinates of beacons
                new double[]{0, 0, 1, 1},      //y "
                new double[]{2, 2, 2, 2},      //z "
                new double[]{2.111244657, 2.282839022, 2.031589033, 2.209378646}   //The distance reported by each beacon
        );

        for (int i = 0; i < 3; i++) {
            assertTrue("Trilateration coordinate is correct", Math.abs(result.get(i) - expected[i]) < 0.000001);
        }
    }

    //5 beacons placed in random positions, but their distances are noisy
    public void testNoisy() throws BeaconError {
        double[] expected = {.877, .665, 0};

        Point3D result = trilatRunHelper(
                new double[]{1.1, 0, 1, 0, 4},
                new double[]{0.1, 0, 1, 1, 4},
                new double[]{1.5, 2.3, 2, 1, 2},
                new double[]{1.64550334202622, 2.52500476178802, 2.03585577254456, 1.37655914735018, 4.98681775088675}  //Somewhat noisy distances
        );
        for (int i = 0; i < 3; i++) {
            assertTrue("Trilateration coordinate is close enough", Math.abs(result.get(i) - expected[i]) < 0.1);
        }
    }

    //TWENTY beacons
    //It is complete bluetooth anarchy in this user's room right now
    public void testNoisyHuge() throws BeaconError {
        double[] expected = {.877, .665, 0};

        //Beacons placed in completely random positions
        //Distances are also noisy
        Point3D result = trilatRunHelper(
                new double[]{0.845112476215237e-2, 1.14297964348494, -0.207876680961557e-1, 1.88017308078449, .265183287704836, .864322683828219, .256005382644264, 1.97714139671262, .651834630972347, 1.00410012887403, -.921373024509359, 1.09040993536071, .311718423004093, -.244594990245632, .112657662671979, -.216595401219630, -.150359338389866, 1.27009473039531, .835695773451052, -.909215102539883},
                new double[]{8.69587181757857, -1.10866322429944, 1.52991841523379, 1.22884893203545, 1.78434144619135, 3.58975218254698, 2.54922988151990, 2.65007163026388, 3.88827352493541, 2.47467119756452, -.147113320754151, .964302705863695, -3.08576837401448, 1.69840919947517, .762757841486923, -1.06097573715729, -2.04371587045021, -4.54266706666593, .287550933194703, -.854493579499496},
                new double[]{-.511624796803494, -2.12170779617452, -1.91736031081189, 2.36499612437790, 3.03117859621799, 0.491661900482522e-1, 1.25227416433019, 1.29568900609844, 3.86555948692895, .220641959923629, -3.62718593822006, -2.42545387943314, .860496275543384, 2.68421449207342, .501273412704400, -4.25038749873887, -3.14258653490982, -3.88697210683071, 2.86499588849480, .906341240688641},
                new double[]{8.13483664664817, 2.76113415857844, 2.24754930649240, 2.65827425967542, 3.26889908890460, 2.93058069384487, 2.37674034081026, 2.62139781328668, 5.01506795248720, 1.82744480241914, 4.13221129802146, 2.42355625896287, 3.78186664448935, 3.11175142944014, .921014658444426, 4.73844726305438, 4.27921730751287, 6.48063688717400, 2.88174180877585, 2.52332429064023}
        );

        for (int i = 0; i < 3; i++) {
            assertTrue("Trilateration coordinate is close enough", Math.abs(result.get(i) - expected[i]) < 0.1);
        }
    }

    private Point3D trilatRunHelper(double[] xs, double[] ys, double[] zs, double[] dists) throws BeaconError {
        int l = xs.length;
        if (!(ys.length == l && zs.length == l && dists.length == l))
            throw new BeaconError("Bad lists");
        BeaconManager mngr = new BeaconManager();
        for (int i = 0; i < l; i++) {
            //Normally the 4th parameter of new Beacon() is omitted, but this is for testing
            Beacon add = new Beacon(xs[i], ys[i], zs[i]);
            add.setDistance_test(dists[i]);
            mngr.addBeacon(Integer.toString(i),add);
        }
        return mngr.doTrilateration();
    }
}


