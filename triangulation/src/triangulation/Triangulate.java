package triangulation;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import java.applet.*;
import java.awt.*;
import java.lang.Math;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import triangulation.Point;
import java.util.Random;

/**
 * @author Domino
 */
public class Triangulate {

    public int amount = 20; //kolko bodov chcem v poli
    public Point[] point_cloud = new Point[amount];  //array of points2d  // ID, X, Y, used(bool)
    public Edge[] edges = new Edge[30 * amount - 3];  //array of edges, hran je len tolko kolko je teoreticky nekonvexnych hran //akbyze potrebujeme konvexne hrany rozmyslat -> je potrebne zvysit kapacitu pola, o neviem kolko :) (pripadne vyrobit druhe pole urcene len an tieto hrany)
    public Edge[] convex = new Edge[100];  //array of edges, hrany konvexne
    //public Edge[] edges = new Edge[100];  //array of edges, hran je len tolko kolko je teoreticky nekonvexnych hran //akbyze potrebujeme konvexne hrany rozmyslat -> je potrebne zvysit kapacitu pola, o neviem kolko :) (pripadne vyrobit druhy LIST urceny len na tieto hrany)
    public Circle[] circles = new Circle[amount];
    ArrayList<Circle> circlesA = new ArrayList<Circle>();
    ArrayList<Edge> convexA = new ArrayList<Edge>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //System.out.println((int)(Math.random() * Math.random() * 100));
        System.out.println("////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("Triangulation algorithm of Boris Nikolaevich Delaunay, code by Dominik Januvka. 2011");
        System.out.println("Version 2.0 , NEWS: implemented new triangulation method");
        System.out.println("////////////////////////////////////////////////////////////////////////////////////");
        // volanie spustenia aplikacie
        Triangulate triangulate = new Triangulate();

    }
    /*
     * samotne spustenie
     */

    public Triangulate() {
        getRandomPoints(amount);  //neake mracno bodov v 2d
//        for (int i = 0; i < point_cloud.length; i++) {
//            System.out.println(point_cloud[i].toString()); //show points in point_cloud
//        }

        // zvolime si nahodne startovaci bod  ---alebo
//        int startPointID = getRandomStartPoint(amount);
//        System.out.println("first point: " + point_cloud[startPointID]);

        //--- alebo... zvolime si optimalny bod podla metriky
        //parameter 1-ked chcem sortovat pole pointov,0-nechcem nic sortovat
        int startPointID = getOptimalStartPoint(0, point_cloud, amount);
        System.out.println("first OPTIMAL point: " + point_cloud[startPointID]);

        
        // spravime prvy trojuholnik!!!
        if (amount >= 3) {
            firstTriangle(startPointID);
        } else {
            System.out.println("nedostatocny pocet bodov pre triangulaciu");
        }

        /* teraz treba hladat najblizsi bod k trojuhoniku
        a - porovnat vzdialenost kazdej hrany s najblizsim najdenym vybranym bodom   //*** optimalizacia
        b - circumcircles
        c - spirala, pozerat +1 bod hore, dole, doprava...
         */
        triangulate();  //ostatne 3uholniky
        
        //dokreslime konvexne trojuholniky
        konvex();

        //opisana kruznica
        //circumcircle(Point p1,Point p2,Point p3);
        
        //otvorime si okienko
        Show gui = new Show();
        gui.setVisible(true);
        gui.Kresli(point_cloud, edges, circlesA);
    }

    /*
     * Generates a set of random points to triangulate.
     */
    private void getRandomPoints(int amount) {
//        point_cloud[0] = new Point(0,1,1);
//        point_cloud[1] = new Point(1,2,3);
//        point_cloud[2] = new Point(2,3,2);
//        point_cloud[3] = new Point(3,10,11);
//        point_cloud[4] = new Point(4,11,10);
//        point_cloud[5] = new Point(5,12,10);
//        point_cloud[6] = new Point(6,17,12);
//        point_cloud[7] = new Point(7,19,15);
//        point_cloud[8] = new Point(8,18,16);
//        point_cloud[9] = new Point(9,14,13);

//        point_cloud[0] = new Point(0,10,10);
//        point_cloud[1] = new Point(1,10,20);
//        point_cloud[2] = new Point(2,10,30);
//        point_cloud[3] = new Point(3,20,10);
//        point_cloud[4] = new Point(4,20,20);
//        point_cloud[5] = new Point(5,20,30);
//        point_cloud[6] = new Point(6,30,10);
//        point_cloud[7] = new Point(7,30,20);
//        point_cloud[8] = new Point(8,30,30);
//        point_cloud[9] = new Point(9,40,10);

        for (int i = 0; i < point_cloud.length; i++) {
            point_cloud[i] = new Point(i, (int) (Math.random() * Math.random() * 100), (int) (Math.random() * Math.random() * 100));
        }

    }

    /*
     * Generates a random ID of start point, to start triangulation.
     */
    private int getRandomStartPoint(int amount) {
        Random randomGenerator = new Random(); //startovaci bod, chcem nahodne cislo od 0 - amount, startovaci bod
        return randomGenerator.nextInt(amount);
    }

    /*
     * make first triangle
     */
    private void firstTriangle(int startPointID) {
        point_cloud[startPointID].setUsed();
        Float dist = new Float(0);
        Float dist_last = Float.MAX_VALUE;
        int point2 = 0;
        int point3 = 0;
        int y = 0;
        int[] last = new int[5];

        for (int i = 0; i < point_cloud.length; i++) {
            if (!(point_cloud[i].isUsed())) {     //vsetky okrem prveho vybrateho //je pouzity? ak NIE pokracuj alg, Ak ANO
                /*porovname vsetky vzdialenosti bodov*/
                dist = distance(point_cloud[startPointID], point_cloud[i]);
                //System.out.println(dist);
                //System.out.print("->");
                //System.out.println(dist.compareTo(dist_last));

//               if ( i==1 ) {
//                    dist_last = dist;
//                    point2 = 1;
//                }
                //if (dist < dist_last) {
                if (0 >= dist.compareTo(dist_last)) {
                    dist_last = dist;
                    point2 = i;
                }
            }
        }
        System.out.println("second point. distance: " + dist_last + ", point: " + point_cloud[point2].toString());
//        makeEdge(0,startPointID,point2);
        makeEdge(0, point_cloud[startPointID], point_cloud[point2]);
        point_cloud[point2].setUsed();
        //edges[0].setUsed();
//-----------------teraz druha hrana----------------------------------------
        dist_last = Float.MAX_VALUE;

        for (int i = 0; i < point_cloud.length; i++) {
            if (!(point_cloud[i].isUsed())) {     //vsetky okrem prveho vybrateho
                /*porovname vsetky vzdialenosti bodov*/
                dist = distanceFromEdge(edges[0], point_cloud[i]);
                //System.out.println(dist);
                //System.out.print("->");
                //System.out.println(dist.compareTo(dist_last));
//                if ( i==2) {
//                    dist_last = dist;
//                    point3 = 2;
//                }
                //if (dist < dist_last) {  // XXX pouzit ine porovnavanie
                if (0 >= dist.compareTo(dist_last)) {  // XXX pouzit ine porovnavanie
                    dist_last = dist;
                    point3 = i;
                }
            }
        }
        System.out.println("third point. distance from edge: " + dist_last + ", point: " + point_cloud[point3].toString());
        
        //TODO: doplnit delaunay circumCircles koli kontrole prekryvania hran
        int bool = circleHasPoint(point_cloud[startPointID], point_cloud[point2], point_cloud[point3]);
        while (bool != -1) { //totot tu sa musi opakovat lebo ak nie nenajde ESTE vhodnejsie one, tieto, vertexy :) uz nevladzem
        //if (bool != -1) {

                System.out.println("_"+point3+"_"+bool);
//                System.out.println(last[y++]+"_"+point3+"_"+bool);
                System.out.println("_");

           
////osetrenie podla poctu krokov
//if(y++== 3 ) break;

//osetrenie podla posledneho vyskytu
if(y>=2) {if(last[y-2]==bool)break;}
last[y++] = bool;


                point3 = bool;
                System.out.println("!!repaired!!point!!. distance from edge: " + dist_last + ", point: " + point_cloud[point3].toString());
                bool = circleHasPoint(point_cloud[startPointID], point_cloud[point2], point_cloud[point3]);
                //TODO: osetrit ak su rovnako blizko --->> ze ak su na jednej kruznici -->> hrozi zacyklenie.
            }
        System.out.println("-------------------------");
        
        
        makeEdge(1, point_cloud[startPointID], point_cloud[point3]);
        makeEdge(2, point_cloud[point2], point_cloud[point3]);
        point_cloud[point3].setUsed();
        //edges[1].setUsed();
        //edges[2].setUsed();
    }


//------------------------------------------------------------------------------
    /*
     * samotna triangulacia
     */
    private void triangulate() {
        
        

        
        
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
        return (float) Math.sqrt((double) (dx * dx + dy * dy));
    }

    /**
     * distance from EDGE
     */
    public float distanceFromEdge(Edge e, Point b) {
        float dx, dy;
        float x, y;

        x = (e.l.getX() + e.r.getX()) / 2;
        y = (e.l.getY() + e.r.getY()) / 2;

        dx = x - b.getX();
        dy = y - b.getY();
        //System.out.println((float)Math.sqrt((double)(dx * dx + dy * dy)));
        return (float) Math.sqrt((double) (dx * dx + dy * dy));
    }

    /*
     * sprav hranu!
     */
//    private void makeEdge(int i, int left, int right) {
//        edges[i] = new Edge(left, right);
//    }
    private void makeEdge(int i, Point left, Point right) {
        edges[i] = new Edge(left, right);

        float x, y;
        x = (left.getX() + right.getX()) / 2;
        y = (left.getY() + right.getY()) / 2;
        Point s = new Point((int) x, (int) y);
        edges[i].midpoint(s);
    }

    /*
     * delaunay circumcircle,
     * input 3 body ,
     * return je najblizsi kandidat ak existuje vo vnutri opisanej kruznice.
     */
    private int circleHasPoint(Point a, Point b, Point c) {
        // TODO: code DELAUNAY circumcircle right here !!!
        Circle cc = circumcircle(a, b, c);
        
        for (int i = 0; i < point_cloud.length; i++) {
            if (i == a.getID() || i == b.getID() || i == c.getID() ) {}
            else{
//                if (!(point_cloud[i].isUsed())) {
//                if (true) {
                    if (cc.isInside(point_cloud[i])) {
//                        if (cc.isEmpty(point_cloud,i)) {
//                            return i;
//                        } else {
//                            circleHasPoint(a, b, point_cloud[i]); //zacykli sa ak chyti 2 body na tej istej kruznici
                            return i;
//                        }
//                    }
                }
            }
        } return -1;
    }

    /*
     * Compute the circle defined by three points (circumcircle).
     */
    private Circle circumcircle(Point p1,Point p2,Point p3) {
	float premenna;
        float circleX, circleY;
        circleX = circleY = 0;

	premenna = crossProduct(p1, p2, p3);
	if (premenna != 0.0)
	    {
                float p1Sq, p2Sq, p3Sq;
                float num, den;

		p1Sq = p1.getX() * p1.getX() + p1.getY() * p1.getY();
		p2Sq = p2.getX() * p2.getX() + p2.getY() * p2.getY();
		p3Sq = p3.getX() * p3.getX() + p3.getY() * p3.getY();

		num = p1Sq*(p2.getY() - p3.getY()) + p2Sq*(p3.getY() - p1.getY()) + p3Sq*(p1.getY() - p2.getY());
		circleX = num / (2.0f * premenna);
		num = p1Sq*(p3.getX() - p2.getX()) + p2Sq*(p1.getX() - p3.getX()) + p3Sq*(p2.getX() - p1.getX());
		circleY = num / (2.0f * premenna);
        
                premenna = distance(new Point( (int)circleX, (int)circleY), p1);
	    }

	// Radius
	//r = c.distance(p1);
        circlesA.add(new Circle(premenna,circleX,circleY)); //adding value to ArrayList
        System.out.print(".");
        return new Circle(premenna,circleX,circleY);
    
    }

    static float crossProduct(Point p1, Point p2, Point p3) {
	float u1, v1, u2, v2;

	u1 =  p2.getX() - p1.getX();
	v1 =  p2.getY() - p1.getY();
	u2 =  p3.getX() - p1.getX();
	v2 =  p3.getY() - p1.getY();

	return u1 * v2 - v1 * u2;
    }

    private void konvex() {
        System.out.println("//TODO: implement THIS!");
    }

/**
 * vyriesi problem optimalneho startu
 * @param amount
 * @param point_cloud
 * @return
 */
    private int getOptimalStartPoint(int parameter, Point[] pc, int amount) {
        metrika m = new metrika(pc, amount);
//daj mi optimalny bod = pozor! on iba vypocita MIN vzdialenost a AVG vzdialenost medzi bodmi a vrati 1.BOD
        int i = m.getPoint();
//zorad pole podla ... toho co je v      public int compareTo(Object obj) {
        if (parameter==1) {
            point_cloud = m.sort();   
        }
//        for (int t = 0; t < point_cloud.length; t++) {
//            System.out.println(">>"+point_cloud[t].getID()+">>"+point_cloud[t].getMin());
//        }
        return i;
    }
}



















//
//        // edge startujeme pocitat od 3
//        Float dist = new Float(0);
//        Float dist_last = Float.MAX_VALUE;
//        int point = 0;
//        int edge = 0;
//        int y = 0;
//        int z = 0;
//        int bool = 0;
//        boolean b = false;
//        int hrana1;
//        int hrana2;
//
//
//
//
//        for (int k = 3; k < edges.length; k++) {
//            dist_last = Float.MAX_VALUE;
//            //for ( y = 0; y < edges.length; y++) {
//
//            y=0;
//            while (edges[y] != null /* && !(edges[y].isUsed()) */ ) {
//            //for (y = 0; y < k; y++) {
//                for (int j = 0; j < point_cloud.length; j++) {
//                    if (!(point_cloud[j].isUsed())) {
//                        dist = distanceFromEdge(edges[y], point_cloud[j]);
//                        if (0 >= dist.compareTo(dist_last)) {  //XXX pouzit ine porovnavanie
//                            dist_last = dist;
//                            point = j;
//                            edge = y;
//                        }
//                    }
//                }//nasiel som najblizsi bod k edge-y   ...ale musim skontrolovat ci neexistuje taka hrana co ma k bodu este blizsie
//                //aj tak sa prekryvaju lebo on MUSI najst ku kazdej hrane bod aj keby neviem co! //TODO: osetrit
//
//                z = 0;
//                while (edges[z] != null /*&& !(edges[z].isUsed())*/) {
//                //for (z = 0; z < k; z++) {
//                //    if (z != y /* && !(edges[z].isUsed()) */) {
//                        dist = distanceFromEdge(edges[z], point_cloud[point]);
//                        if (0 >= dist.compareTo(dist_last)) {  //XXX pouzit ine porovnavanie
//                            dist_last = dist;
//                            edge = z;
//                        }
//               //     }
//                    z++;
//                }
//                //TODO: doplnit delaunay circumCircles koli kontrole prekryvania hran
////                bool = circleHasPoint(edges[edge].l, edges[edge].r, point_cloud[point]);
////                if (bool != -1) {
////                    if(point_cloud[bool].isUsed()){
////                        System.out.println("!!!!!!!");
////                        makeEdge(k, point_cloud[point], point_cloud[point]);
////                        // distance(point_cloud[point], edges[edge].r) < distance(point_cloud[point],edges[edge].l)
////                        if (distance(point_cloud[point], edges[edge].r) < distance(point_cloud[point],edges[edge].l)) {   //blizsi bod (pozri papier) aby sa neprekryvali hrany
////                            makeEdge(++k, point_cloud[point], edges[edge].r);  //R
////                        } else {
////                            makeEdge(++k, point_cloud[point], edges[edge].l);  //L
////                        }
////                        point_cloud[point].setUsed();
////                        edges[edge].setUsed();
////                        break;
////                    }else{
////                        point = bool;
////                    }
////                }
//
////                bool = circleHasPoint(edges[edge].l, edges[edge].r, point_cloud[point]);
////                while (bool != -1) {
////                    //if (bool != -1) {
////                    if (point_cloud[bool].isUsed()) {
////                        System.out.println("!!!!!!!");
////                        makeEdge(k, point_cloud[point], point_cloud[bool]);
////                        // distance(point_cloud[point], edges[edge].r) < distance(point_cloud[point],edges[edge].l)
////                        if (distance(point_cloud[point], edges[edge].r) < distance(point_cloud[point], edges[edge].l)) {   //blizsi bod (pozri papier) aby sa neprekryvali hrany
////                            makeEdge(++k, point_cloud[point], edges[edge].r);  //R
////                        } else {
////                            makeEdge(++k, point_cloud[point], edges[edge].l);  //L
////                        }
////                        point_cloud[point].setUsed();
////                        edges[edge].setUsed();
////                        //break;
////                    } else {
////                        point = bool;
////
//////                    if (!(edges[edge].isUsed())) {   //toto je asi zbytocne, asi, zrejme neviem...
////                        makeEdge(k, point_cloud[point], edges[edge].l);  //L
////                        makeEdge(++k, point_cloud[point], edges[edge].r);  //R
////                        point_cloud[point].setUsed();
////                        edges[edge].setUsed();
//////                   } else {
//////                        System.out.println(">>>>toto nemalo nastat<<<<<<");
//////                    }
////                    }
////                    bool = circleHasPoint(edges[edge].l, edges[edge].r, point_cloud[point]);
////                }
//                y++;
//            }
//
//            int[] last = new int[1000];
//            int i=0;
//            int ii = 0;
//            int fff = point;
//
//                //TODO: doplnit delaunay circumCircles koli kontrole prekryvania hran
//            bool = circleHasPoint(edges[edge].l, edges[edge].r, point_cloud[point]);
//            
//            while (bool != -1) {   //rekurzia ci nema opisana kruznica este dalsie body v sebe
//                //if (bool != -1) {
//                System.out.println("*"+point+"_"+bool);  // tu sa to zacyCYkli... :-/
//                //osetrime dvojmo, dve tri dozadu čeknem PLUS viac krat, cyklus, jak jaky je pocetvšetkych bodov sa nevykona
//
//                if(i>=3) {if(last[i-2]==bool || last[i-3]==bool)break;}
//                System.out.println("jeden"+ i);
//
//                if(ii++==point_cloud.length)break;
//
//                last[i++] = bool;
//                System.out.println("dvaaa"+ i);
//                point = bool;
//                bool = circleHasPoint(edges[edge].l, edges[edge].r, point_cloud[point]);
//            }//else{
//
//
//            if (point_cloud[point].isUsed()) {
//                System.out.println("!");          
////                makeEdge(k, point_cloud[fff], point_cloud[point]);    // toto by to nemalo robit :-(
//                makeEdge(k, point_cloud[point], edges[edge].r);  //TODO: este osetrit ci uz existuje.
//                hrana1 = k;
//                makeEdge(++k, point_cloud[point], edges[edge].l);
//                hrana2 = k;
//                if (distanceFromEdge(edges[hrana1],point_cloud[fff]) < distanceFromEdge(edges[hrana2],point_cloud[fff])) {   //blizsi bod (pozri papier) aby sa neprekryvali hrany
//                    makeEdge(++k, point_cloud[fff], edges[hrana1].r);  //R
//                    makeEdge(++k, point_cloud[fff], edges[hrana1].l);  //L
//                    edges[hrana1].setUsed();
//                } else {
//                    makeEdge(++k, point_cloud[fff], edges[hrana2].r);  //R
//                    makeEdge(++k, point_cloud[fff], edges[hrana1].l);  //L
//                    edges[hrana2].setUsed();
//                }
//
//
//                point_cloud[fff].setUsed();
//                point_cloud[point].setUsed();
//                edges[edge].setUsed();
//                //break;
//            } else {
//                makeEdge(k, point_cloud[point], edges[edge].l);  //L
//                makeEdge(++k, point_cloud[point], edges[edge].r);  //R
//                point_cloud[point].setUsed();
//                edges[edge].setUsed();
//            }
//
//
//
//
////----------------------------
////----------------------------
//
//        }
// 
    