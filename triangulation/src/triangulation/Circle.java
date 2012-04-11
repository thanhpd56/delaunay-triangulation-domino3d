/*
 * Trieda Definicie kruznice opisanej
 */
package triangulation;

/**
 * Trieda Definicie kruznice opisanej
 * Circle - kruznica, opisana trojuholniku, ma stred S a polomer
 * @author Domino
 * 
 */
public class Circle {
    private double x;
    private double y;
    private double z;
    private double  r;
    Point[] point_cloud;

    Circle(double R, double circleX, double circleY, double circleZ) {
        this.x = circleX;
        this.y = circleY;
        this.z = circleZ;
        this.r = R;
    }

    /*
     * metoda na urcenie ci dany bod je vo vnutri kruznice
     * false, //ked nie je vo vnutri 3uholnika dalsi bod
     */
    public boolean isInside(Point p) {
        Double a = this.distance(p); // a=round(a,4);   //zaokruhlime na 4 desatinne cisla
        Double b = this.getR() ;  
        if (0 > a.compareTo(b)){
//            if (round(a,6).compareTo(round(b,6)) == 0 ) {
////            if (a.compareTo(b) == 0 ) {
//                System.out.println("    trolololo    ..."); //ked su body na jednej kruznici
//            }
            return true;
        }
        else
            return false; //ked nie je vo vnutri 3uholnika dalsi bod
    }
    
    
//********-------
        public int isInside1(Point p) {
        Double a = this.distance(p);
        Double b = this.getR();
        if (0 > a.compareTo(b)){
            if (round(a,6).compareTo(round(b,6)) == 0 ) {
//                System.out.println("    trolololo    ..."); 
                return 0; //ked su body na jednej kruznici
            }
            return 1; //true
        }
        else
            return -1; //false //ked nie je vo vnutri 3uholnika dalsi bod
    }

    /**
     * distance from STRED kruznice to Point A
     */
    public double distance(Point a) {
        double dx, dy, dz;

        dx = a.getX() - this.getX();
        dy = a.getY() - this.getY();
        dz = a.getZ() - this.getZ();
        //System.out.println((double)Math.sqrt((double)(dx * dx + dy * dy)));
//        return Math.sqrt((double) (dx * dx + dy * dy));
        return Math.sqrt((double) (dx * dx + dy * dy + dz * dz));
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
     * @return the y
     */
    public double getZ() {
        return z;
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
    
//to string
    @Override
    public String toString (){
        return "CCCCCC: "+x+","+y+","+z;
    }


}
