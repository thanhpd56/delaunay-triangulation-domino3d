package triangulation;

/**
 *
 * @author Domino
 * Circle - kruznica, opisana trojuholniku, ma stred S a polomer
 */
public class Circle {
    private double x;
    private double y;
    private double  r;
    Point[] point_cloud;

    Circle(double R, double circleX, double circleY) {
        this.x = circleX;
        this.y = circleY;
        this.r = R;
    }

    /*
     * metoda na urcenie ci dany bod je vo vnutri kruznice
     */
    public boolean isInside(Point p) {
        Double a = this.distance(p);
//        a=round(a,4);   //zaokruhlime na 4 desatinne cisla
        Double b = this.getR();
//        b=round(b,4);  //zaokruhlime --//--
	if (0 > a.compareTo(b)){
//System.out.println("/"+a+"_<_"+b+" "+(0 > a.compareTo(b)));
            
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
    public double distance(Point a) {
        double dx, dy;

        dx = a.getX() - this.getX();
        dy = a.getY() - this.getY();
        //System.out.println((double)Math.sqrt((double)(dx * dx + dy * dy)));
        return Math.sqrt((double) (dx * dx + dy * dy));
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @return the r
     */
    public double getR() {
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
    private Double round(Double Rval, int Rpl) {
        double p =  Math.pow(10, Rpl);
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return  tmp / p;
    }


}
