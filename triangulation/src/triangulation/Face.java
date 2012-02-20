package triangulation;

/**
 * face ma tri vrcholy: x, y, z
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
    @Override
    public String toString() {
        return " "+ x +" "+ y +" "+ z +" ";
    }
    
    
}
