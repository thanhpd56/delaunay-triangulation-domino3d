package triangulation;

/**
 *
 * @author Domino
 */
//2D point
public class Point implements Comparable{
    private int ID;
//int
//    private int x;
//    private int y;
//real
    private double  x;
    private double  y;
//metrika
    private double min;
    private double avg;

    private boolean used = false;

    //public Point(int ID,float x,float y){
    public Point(int ID,double x, double y){
        this.ID=ID;
        this.x=x;
        this.y=y;
    }
    public Point(double x,double y){
        this.x=x;
        this.y=y;
    }

    /**
     * compareTo
     * @return
     */
    public int compareTo(Object obj) {
        Double a = new Double(0);
        Double b = new Double(0);

        Point tmp = (Point) obj;
        a=this.min;
        b=tmp.min;

        if (0 >= a.compareTo(b)) {
//        if (this.min < tmp.min) {
            /* instance lt received */
            return -1;
        } else  if (0 <= a.compareTo(b))  /*if (this.min > tmp.min)*/ {
            /* instance gt received */
            return 1;
        }
        /* instance == received */
        return 0;
    }







//get set ID
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
//get set X
    public double  getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
//get set Y
    public double  getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

//used
    public boolean isUsed() {
        return used;
    }
    public void setUsed() {
        this.used = true;
    }
//to string
    public String toString (){
        return "Point: "+ID+","+x+","+y+","+isUsed();
    }

    /**
     * @return the min
     */
    public double getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * @return the avg
     */
    public double getAvg() {
        return avg;
    }

    /**
     * @param avg the avg to set
     */
    public void setAvg(double avg) {
        this.avg = avg;
    }


}
