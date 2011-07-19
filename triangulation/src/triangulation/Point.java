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
    private float x;
    private float y;
//metrika
    private float min;
    private float avg;

    private boolean used = false;

    public Point(int ID,float x,float y){
        this.ID=ID;
        this.x=x;
        this.y=y;
    }
    public Point(float x,float y){
        this.x=x;
        this.y=y;
    }

    /**
     * compareTo
     * @return
     */
    public int compareTo(Object obj) {
        Float a = new Float(0);
        Float b = new Float(0);

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
    public float getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
//get set Y
    public float getY() {
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
        return "point: "+ID+","+x+","+y+","+isUsed();
    }

    /**
     * @return the min
     */
    public float getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(float min) {
        this.min = min;
    }

    /**
     * @return the avg
     */
    public float getAvg() {
        return avg;
    }

    /**
     * @param avg the avg to set
     */
    public void setAvg(float avg) {
        this.avg = avg;
    }


}
