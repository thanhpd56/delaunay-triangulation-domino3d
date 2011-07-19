package triangulation;

/**
 *
 * @author Domino
 * Circle - kruznica, opisana trojuholniku, ma stred S a polomer
 */
public class Circle {
    private float x;
    private float y;
    private float r;
    Point[] point_cloud;

    Circle(float R, float circleX, float circleY) {
        this.x = circleX;
        this.y = circleY;
        this.r = R;
    }

    /*
     * metoda na urcenie ci dany bod je vo vnutri kruznice
     */
    public boolean isInside(Point p) {
        Float a = this.distance(p);
        a=round(a,4);
        Float b = this.getR();
        b=round(b,4);
	if (0 > a.compareTo(b)){
             System.out.println("/"+a+"_"+b+" "+(0 > a.compareTo(b)));
//	if (this.distance(p) < this.getR() )
//if (0 >= dist.compareTo(dist_last)) {
	    return true;
        }
	else
	    return false;
    }

    /**
     * distance from STRED kruznice to Point A
     */
    private float distance(Point a) {
        float dx, dy;

        dx = a.getX() - this.getX();
        dy = a.getY() - this.getY();
        //System.out.println((float)Math.sqrt((double)(dx * dx + dy * dy)));
        return (float) Math.sqrt((double) (dx * dx + dy * dy));
    }

    /**
     * @return the x
     */
    public float getX() {
        return x;
    }

    /**
     * @return the y
     */
    public float getY() {
        return y;
    }

    /**
     * @return the r
     */
    public float getR() {
        return r;
    }

/**
 * @return ci je kruh praaaaaazdny
 */
    public boolean isEmpty(Point[] point_cloud, int id) {
        for (int i = id; i < point_cloud.length; i++) {
            if (this.distance(point_cloud[i]) < this.getR()) {
                return false;
            }
        }
        return true;
    }

/**
 * zaokruhlenie na pozadovany pocet des miest
 * @param Rval
 * @param Rpl
 * @return
 */
    private Float round(Float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float) tmp / p;
    }


}
