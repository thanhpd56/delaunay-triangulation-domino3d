/*
 * Trieda Definicie hrany
 */
package triangulation;


/**
 * Trieda Definicie hrany
 * @author Domino
 */    

/*
 * Edge class. Edges have two vertices 
 * l (left) and r (right). The triangulation representation and
 * the Delaunay triangulation algorithms require edges.
 */
public class Edge {
//    int s, t;
//    int l, r;    // toto je len ID bodu Laveho a potom pRaveho
    Point l, r;    // toto je len ID bodu Laveho a potom pRaveho
    private Point midpoint;    // toto je stred usecky/hrany
    private boolean used = false;
    private Point[] direction = new Point[2];;  //LEFT -->> //RIGHT


//    public Edge(int l, int r) {
//        this.l=l;
//        this.r=r;
//    }
    public Edge(Point l, Point r) {
        this.l=l;
        this.r=r;
        direction[0]=l;  //LEFT   //FROM  -->>
        direction[1]=r;  //RIGHT  //TO    -->>
    }

//nastav stred
    public void midpoint(Point p){
        this.setMidpoint(p);
    }
//daj mi stred
    public Point getMidpoint(){
        return midpoint;
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
        return "edge: "+l.getID()+","+r.getID();
    }

    /**
     * @return the direction
     */
    public Point[] getDirection() {
        return direction;
    }

    /**
     * @param midpoint the midpoint to set
     */
    public void setMidpoint(Point midpoint) {
        this.midpoint = midpoint;
    }

}
