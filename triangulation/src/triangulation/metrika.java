package triangulation;

import com.sun.org.apache.bcel.internal.generic.FLOAD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Vypocet metriky bodov
 * @author Domino
 */
public class metrika {
    private double result;
    private int amount;
//    Point[] point_cloud;
    ArrayList<Point> point_cloud1;
    double[] cloud;


    public metrika(ArrayList point_cloud, int amount) {
        this.point_cloud1=point_cloud;
        this.amount=amount;
        cloud = new double[amount]; //tu su ulozene vsetky vzdialenosti od toho ktoreho bodu
    }

    /**
     * vypocet metriky pre kazdy bod
     * @return 0 zero
     */
    int getPoint() {
        Double tmp = new Double(0);
        Double dist_last = Double.MAX_VALUE;
        int optimal = 0;

        for (int i = 0; i < amount; i++) {
            for (int j = 0; j < amount; j++) {
                //minimalna vzdialenost
                tmp = distance(point_cloud1.get(i), point_cloud1.get(j));
                cloud[j] = tmp;
                if (0 >= tmp.compareTo(dist_last) && tmp != 0.0) {
                    dist_last = tmp;
                }
            }
            for (int k = 0; k < cloud.length; k++) {
                result = result + cloud[k];
//            }result = round(result / cloud.length,4);
            }
            result = result / cloud.length;

//priradime do mracna kazdemu bodu jeho metriku ! :-)
            point_cloud1.get(i).setMin(dist_last);
            point_cloud1.get(i).setAvg(result);
            System.out.println(i + " min/avg " + dist_last + " / " + result);
//            System.out.println(i + " min/avg " + point_cloud1.get(i).getMin() + " / " + point_cloud1.get(i).getAvg());
            dist_last = Double.MAX_VALUE;
        }

//teraz vyberiem najvhodnejsi prvok  >> podla parametra PRIEMERNEJ vzdialenosti // da sa zmenit
//asi zbytocna fcia pri zapnutom SORT-e, lebo stale vrati prvy bod, pretoze body su uz v liste zotriedene
//todo: vypinat toto pri zapnutom sorte, usetrime cas
//        dist_last = Double.MAX_VALUE;
//        for (int i = 0; i < amount; i++) { 
//            if (dist_last >= (point_cloud1.get(i).getAvg())) {
//                dist_last = (point_cloud1.get(i).getAvg());
//                optimal = i;
//            }
//        }
        
        return optimal;
    }
    
/**
 * SORT
 * @return
 */
    ArrayList<Point> /*Point[]*/ sort() {
//        for (int i = 0; i < point_cloud.length; i++) {
//            System.out.println(point_cloud[i].getID());
//        }

//        Arrays.sort(point_cloud);
        Collections.sort(point_cloud1);
        
//        System.out.println("______");
//        for (int i = 0; i < point_cloud.length; i++) {
//            System.out.println(point_cloud[i].getID());
//        }

        return point_cloud1;
    }

    /**
     * distance from A to B
     * return double
     */
    public double distance(Point a, Point b) {  
        double dx, dy, dz;

        dx = a.getX() - b.getX();
        dy = a.getY() - b.getY();
        dz = a.getZ() - b.getZ();
        
        return  Math.sqrt((double) (dx * dx + dy * dy + dz * dz));
    }

    /**
     * zaokruhlenie na pozadovany pocet des miest
     * @param Rval
     * @param Rpl
     * @return
     */
    private Double round(Double Rval, int Rpl) {
        double p = Math.pow(10, Rpl);
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return  tmp / p;
    }

}
