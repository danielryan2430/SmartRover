package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

//Object oriented programming 101!
public class Point3D {
    private double[] vec = {0, 0, 0};

    public Point3D(double x, double y, double z) {
        vec[0] = x;
        vec[1] = y;
        vec[2] = z;
    }

    public double get(int index) {
        return vec[index];
    }

    public double x() {
        return get(0);
    }

    public double y() {
        return get(1);
    }

    public double z() {
        return get(2);
    }

    public String toString(){
		return String.format("%.3f, %.3f, %.3f",vec[0],vec[1],vec[2]);
    }
}
