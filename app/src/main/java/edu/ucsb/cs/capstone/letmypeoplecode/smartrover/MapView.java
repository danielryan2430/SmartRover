package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by dimberman on 2/17/14.
 */
public class MapView extends ImageView {
    private ArrayList<Paint> beacons = new ArrayList<Paint>();
    private ArrayList<Paint> rovers = new ArrayList<Paint>();

    private Matrix matrix;
    int numBeacons = 4;
    int numRovers = 1;
    int x = 150;
    int y = 200;
    PointF goal;

    public MapView(Context context) {
        super(context);
        sharedTouchView(context);

    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedTouchView(context);

    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedTouchView(context);

    }

    public void sharedTouchView(Context context) {
        matrix = new Matrix();
        //setImageMatrix(matrix);
        for (int i = 0; i < numBeacons; i++)
            beacons.add(new Paint());
        for (int i = 0; i < numRovers; i++)
            rovers.add(new Paint());
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < numBeacons; i++) {
            beacons.get(i).setColor(Color.RED);
            beacons.get(i).setStrokeWidth(1);
        }
        for (int i = 0; i < numRovers; i++) {
            rovers.get(i).setColor(Color.BLUE);
            rovers.get(i).setStrokeWidth(1);
        }
        //beacons.get(0).setAlpha(80);
        //System.out.println(matrix);
        canvas.drawCircle(50, 50, 30, beacons.get(0));
        canvas.drawCircle(getWidth() - 50, getHeight() - 50, 30, beacons.get(1));
        canvas.drawCircle(50, getHeight() - 50, 30, beacons.get(2));
        canvas.drawCircle(getWidth() - 50, 50, 30, beacons.get(3));
        canvas.drawCircle(x,y, 20, rovers.get(0));
        this.setImageMatrix(matrix);
        changeValue();
    }

    public void changeValue(){
        x=(x+5)%getWidth();
        y=(y+5)%getHeight();
        invalidate();
    }


}
