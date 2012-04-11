/*
 * Trieda Definicie polygonu
 */
package triangulation;

/**
 *  Trieda Definicie polygonu face ma tri vrcholy: x, y, z
 * @author Domino
 */
class Face {
    int x, y, z;
//    public Face(Point x, Point y, Point z) {
    /**
     * polygon z 3 stran urceny objektami bodov
     * @param x
     * @param y
     * @param z 
     */
    Face(Point x, Point y, Point z) {
        this.x = x.getID() +1;
        this.y = y.getID() +1;
        this.z = z.getID() +1;
    }
/**
     * polygon z 3 stran urceny ID bodov
     * @param x
     * @param y
     * @param z 
     */
    Face(int x, int y, int z) {
        this.x = x +1;
        this.y = y +1;
        this.z = z +1;
    }

    /**
     * @return the face
     */
    @Override
    public String toString() {
        return " "+ x +" "+ y +" "+ z +" ";
    }
    
    
}
