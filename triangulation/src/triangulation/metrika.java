package triangulation;

import com.sun.org.apache.bcel.internal.generic.FLOAD;
import java.util.Arrays;

/**
 *
 * @author Domino
 */
public class metrika {
    private float result;
    private int amount;
    Point[] point_cloud;
    float[] cloud;


    public metrika(Point[] point_cloud, int amount) {
        this.point_cloud=point_cloud;
        this.amount=amount;
        cloud = new float[amount];
    }

    int getPoint() {
        Float tmp = new Float(0);
        Float dist_last = Float.MAX_VALUE;

        for (int i = 0; i < amount; i++) {
            for (int j = 0; j < amount; j++) {
                //minimalna vzdialenost
                tmp = distance(point_cloud[i], point_cloud[j]);
                cloud[j] = tmp;
                if (0 >= tmp.compareTo(dist_last) && tmp != 0.0) {
                    dist_last = tmp;
                }
            }
            for (int k = 0; k < cloud.length; k++) {
                result = result + cloud[k];
            }result = round(result / cloud.length,4);

            point_cloud[i].setMin(dist_last);
            point_cloud[i].setAvg(result);
//  System.out.println(i+"_"+dist_last+" "+result);
            dist_last = Float.MAX_VALUE;
        }

        
        return 0;
    }
/**
 * SORT
 * @return
 */
    Point[] sort() {
//        for (int i = 0; i < point_cloud.length; i++) {
//            System.out.println(point_cloud[i].getID());
//        }

        Arrays.sort(point_cloud);
        
//        System.out.println("______");
//        for (int i = 0; i < point_cloud.length; i++) {
//            System.out.println(point_cloud[i].getID());
//        }

        return point_cloud;
    }


    /**
     * distance from A to B
     * return float
     */
    public float distance(Point a, Point b) {
        float dx, dy;

        dx = a.getX() - b.getX();
        dy = a.getY() - b.getY();
        //System.out.println((float)Math.sqrt((double)(dx * dx + dy * dy)));
        return round((float) Math.sqrt((double) (dx * dx + dy * dy)),4);
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
