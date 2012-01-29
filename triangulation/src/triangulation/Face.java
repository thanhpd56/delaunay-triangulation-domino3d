package triangulation;

/**
 * face ma tri vrcholy
 * @author Domino
 */
class Face {
    int x, y, z;
    public Face(Point x, Point y, Point z) {
        this.x = x.getID() +1;
        this.y = y.getID() +1;
        this.z = z.getID() +1;
    }

    /**
     * @return the face
     */
    public String getFace() {
        return " "+ x +" "+ y +" "+ z +" ";
    }
    
    
}
