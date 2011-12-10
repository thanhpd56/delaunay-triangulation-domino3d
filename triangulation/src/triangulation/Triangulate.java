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
import sun.org.mozilla.javascript.internal.ast.Jump;

/**
 * @author Dominik Januvka 2011
 */
public class Triangulate {
    
//----->>  interaface
    public int amount = 20; //kolko bodov chcem/mam v poli
    public boolean startRandom ;//= true; //nahodny start bod, false= optimalny start bod
    public boolean collapse ;//= true; //nahodny start bod, false= optimalny start bod
    public boolean sort ;  //sort podla metriky, 1-ano, 0-nie-nesortuj nic
    UIfc ui;
    int loading = 1;
//-----<<  interaface
    
    public Point[] point_cloud ;//= new Point[amount];  //array of points2d  // ID, X, Y, used(bool)
///*zle*/    public Edge[] edges ;//= new Edge[30 * amount - 3];  //array of edges, hran je len tolko kolko je teoreticky nekonvexnych hran //akbyze potrebujeme konvexne hrany rozmyslat -> je potrebne zvysit kapacitu pola, o neviem kolko :) (pripadne vyrobit druhe pole urcene len an tieto hrany)
    public Edge[] convex = new Edge[100];  //array of edges, hrany konvexne
/*zle*/    //public Edge[] edges = new Edge[100];  //array of edges, hran je len tolko kolko je teoreticky nekonvexnych hran //akbyze potrebujeme konvexne hrany rozmyslat -> je potrebne zvysit kapacitu pola, o neviem kolko :) (pripadne vyrobit druhy LIST urceny len na tieto hrany)
    public Circle[] circles ;//= new Circle[amount];
    ArrayList<Circle> circlesA = new ArrayList<Circle>();
    ArrayList<Edge> convexA = new ArrayList<Edge>();
    ArrayList<Edge> edges1 = new ArrayList<Edge>();
    ArrayList<Point> point_cloud1 = new ArrayList<Point>();  //array of points2d  // ID, X, Y, used(bool)
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //System.out.println((int)(Math.random() * Math.random() * 100));
        System.out.println("////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("Triangulation algorithm of Boris Nikolaevich Delaunay, code by Dominik Januvka. 2011");
        System.out.println("Version 2.13 , NEWS: new 3angulation method");
        System.out.println("////////////////////////////////////////////////////////////////////////////////////");
        // volanie spustenia aplikacie
        Triangulate triangulate = new Triangulate();

    }
    
    /**
     * samotne spustenie
     */
    public Triangulate() {
        //spustime user interface pre nastavenie 
        ui = new UIfc(null, true);
        ui.setVisible(true);
        
        amount = ui.getAmount();
        startRandom = ui.isStartRandom();
        sort = ui.getSort();
        collapse = ui.isCollapse();
        
        
        point_cloud = new Point[amount];
//edges = new Edge[30 * amount - 3];
        circles = new Circle[amount];

        //neake nahodne mracno bodov v 2d
        getRandomPoints(amount);

        
        //blizke body spojim podla parametru    (Double) ui.getTolerance().getText()
        if (collapse) {
            point_cloud1 = collapse(Double.parseDouble(ui.getTolerance().getText()));
//            System.out.println("colapse tolerance " + Double.parseDouble(ui.getTolerance().getText()));
            amount = point_cloud1.size();
        } else {
//            for (int i = 0; i < amount; i++) {
//                point_cloud1.add(point_cloud[i]);
//            }
        }

        
//        for (int i = 0; i < point_cloud.length; i++) {
//            System.out.println(point_cloud[i].toString()); //show points in point_cloud
//        }
        
        int startPointID = 0;  //startovaci bod
        if (startRandom) {
            // zvolime si nahodne startovaci bod  ---alebo
            startPointID = getRandomStartPoint(amount);
//            System.out.println("first point: " + point_cloud[startPointID]);
            System.out.println("first RND point: " + point_cloud1.get(startPointID));
        } else {
            //--- alebo... zvolime si optimalny bod podla metriky
            //parameter 1-ked chcem sortovat pole pointov,0-nechcem nic sortovat
            startPointID = getOptimalStartPoint(sort, point_cloud1, amount);
            System.out.println("first OPTIMAL point: " + point_cloud1.get(startPointID));
        }

        
        // spravime prvy trojuholnik!!!
        if (amount >= 3) {
            firstTriangle(startPointID);
        } else {
            System.out.println("nedostatocny pocet bodov pre triangulaciu. KONCIM!");
            System.exit(0);
            return ;  // ???
        }
        
        
        /* teraz treba hladat najblizsi bod k trojuhoniku
        a - porovnat vzdialenost kazdej hrany s najblizsim najdenym vybranym bodom   //*** optimalizacia
        b - circumcircles
        c - spirala, pozerat +1 bod hore, dole, doprava...
         */
        triangulate();  //ostatne 3uholniky
        
        //dokreslime konvexne trojuholniky po krajoch (mozme, NEMUSIME)
        konvex();
        
        
        //otvorime si okienko a nakreslim co to vlastne vyrobilo
        Show gui = new Show();
        gui.setVisible(true);
        gui.Kresli(point_cloud1, edges1, circlesA);
    }

    /**
     * Generates a set of random points to triangulate, nums from 0.0 to 100.0.
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
////        point_cloud[2] = new Point(2,10,30);
//        point_cloud[3] = new Point(3,20,10);
//        point_cloud[4] = new Point(4,20,20);
//        point_cloud[5] = new Point(5,20,30);
//        point_cloud[6] = new Point(6,30,10);
//        point_cloud[7] = new Point(7,30,20);
//        point_cloud[8] = new Point(8,30,30);
//        point_cloud[9] = new Point(9,40,10);
        
//        point_cloud1.add(new Point(0,10,10));
//        point_cloud1.add(new Point(1,10,20));
//        point_cloud1.add( new Point(2,10,30));
//        point_cloud1.add( new Point(3,20,10));
//        point_cloud1.add(new Point(4,20,20));
//        point_cloud1.add( new Point(5,20,30));
//        point_cloud1.add( new Point(6,30,10));
//        point_cloud1.add( new Point(7,30,20));
//        point_cloud1.add( new Point(8,30,30));
//        point_cloud1.add( new Point(9,40,10));

//        for (int i = 0; i < point_cloud.length; i++) {
        for (int i = 0; i < amount; i++) {
//            point_cloud1.add(new Point(i, /*(int)*/ round((Math.random() * Math.random() * 100),4), /*(int)*/ round((Math.random() * Math.random() * 100),10)));
            point_cloud1.add(new Point(i, (Math.random() * Math.random() * 100),  (Math.random() * Math.random() * 100)));
        }

    }

    /**
     * Generates a random ID of start point, to start triangulation.
     */
    private int getRandomStartPoint(int amount) {
        Random randomGenerator = new Random(); //startovaci bod, chcem nahodne cislo od 0 - amount, startovaci bod
        return randomGenerator.nextInt(amount);
    }

    /**
     * make first triangle
     */
    private void firstTriangle(int startPointID) {
        point_cloud1.get(startPointID).setUsed(); //nastavenia priznaku "zneuzitia" tohto bodu musi byt TU na zaciatku aby sme ho uz pri FORe nepouzili
        ui.jProgressBar1.setValue(100*loading++/amount);
        Double dist = new Double(0);
        Double dist_last = Double.MAX_VALUE;
        int point2 = 0;
        int point3 = 0;
        int counter = 0;  //univerzalne pocitadlo, mozem recyklovat podla potreby !

        
//----hladanie 2. bodu a 1. hrany---------
        for (int i = 0; i < point_cloud1.size(); i++) { //porovname vsetky vzdialenosti bodov, prveho vybrateho so vsetkymi, tak najdeme 2. bod trojuholnika
            if (!(point_cloud1.get(i).isUsed())) {     //vsetky okrem prveho vybrateho //je pouzity? ak NIE pokracuj alg, Ak ANO
                dist = distance(point_cloud1.get(startPointID), point_cloud1.get(i));
                if (0 >= dist.compareTo(dist_last)) {
                    dist_last = dist;
                    point2 = i;
                }
            }
        }
        System.out.println("second point. distance: " + dist_last + ", point: " + point_cloud1.get(point2).toString());
        makeEdge(0, point_cloud1.get(startPointID), point_cloud1.get(point2));
        point_cloud1.get(point2).setUsed();
        ui.jProgressBar1.setValue(100*loading++/amount);
        //edges[0].setUsed();
        
//-----hladanie 3. bodu a 2.+3. hrany------------------------------------
        dist_last = Double.MAX_VALUE;

        for (int i = 0; i < point_cloud1.size(); i++) {
            if (!(point_cloud1.get(i).isUsed())) {     //vsetky okrem prvyh 2 vybratehyh
                //porovname vsetky vzdialenosti bodov od 2. bodu, MOZME hladat aj od STREDU KRUZNICE (ak chceme)!!!
                // MOZME zobrat hned PRVY nepouzity z hora lebo ak su optimalne sortovane netreba hladat najblizsi!!!
//                dist = distanceFromEdge(edges[0], point_cloud[i]);
                dist = distanceFromEdge(edges1.get(0), point_cloud1.get(i));
                if (0 >= dist.compareTo(dist_last)) {  
                    dist_last = dist;
                    point3 = i;
                }
            }
        }
        System.out.println("third point. distance from edge: " + dist_last + ", point: " + point_cloud1.get(point3).toString());
        
        //Alg na hladanie vhodnejsieho kandidata!
        int xxx = circleHasPoint(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(point3));
        if (xxx != -1) {
            int yyy = -1;
            ArrayList invalid = new ArrayList(); // declares an array of integers        
            
            while (xxx != -1) {
                if (yyy == -1) {
                    invalid.add(point3); //nastane len raz.
                } else {
                    invalid.add(yyy);   
                }
                if (invalid.contains(xxx)) {
                    point3 = yyy;
                    System.out.println("break!");
                    break;
                } else {
                    yyy = xxx;
                    xxx = circleHasPoint(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(xxx));
                    System.out.println("navstivil som " + yyy + ", novy je " + xxx);
                }
            }

            if (xxx == -1) {   // ak sme nasli nieco vhodnejsie tak to dame do POINT3
                point3 = yyy;
                System.out.println("oprava");
            }
        }
        
        System.out.println("----------koniec prveho 3uholnika---------------");
        
        
        makeEdge(1, point_cloud1.get(startPointID), point_cloud1.get(point3));
        makeEdge(2, point_cloud1.get(point2), point_cloud1.get(point3));
        point_cloud1.get(point3).setUsed();
        ui.jProgressBar1.setValue(100*loading++/amount);
    }


//------------------------------------------------------------------------------
    /**
     * samotna triangulacia
     */
    private void triangulate() {
        int exp = 0;
        int edgeID = 3;
        int xxx;
        Double dist = new Double(0);
        Double dist_last = Double.MAX_VALUE;
        int point = -1;
        boolean koniec = false;
        boolean cyklus = false;
        int point3 = 0;
        
        
//        for (int f = 0; f < point_cloud.length; f++) {
//            point_cloud[f].setUsed();
//        }
        
        for (int i = 0; i < edges1.size(); i++) {
            koniec = false;

            //4 used points only
            for (int j = 0; j < point_cloud1.size(); j++) {
                if (point_cloud1.get(j).isUsed()) {
                    if (edges1.get(i).l == point_cloud1.get(j) || edges1.get(i).r == point_cloud1.get(j)) { //todo: nie som si isty ci takto to mozem porovnavat
                    } else {
                        xxx = circleHasPoint(edges1.get(i).l, edges1.get(i).r, point_cloud1.get(j));
                        
                        cyklus = false;
                        if (xxx != -1) {
/* //----->>
                            int yyy = -1; //tu je zapamatany stale najvyhodnejsi bod !!!
                            ArrayList invalid = new ArrayList(); // invalidne body

                            while (xxx != -1) {
                                if (yyy == -1) {
                                    invalid.add(j); //nastane len raz.
                                } else {
                                    invalid.add(yyy);   
                                }
                                if (invalid.contains(xxx)) {
                                    point3 = yyy;
                                    System.out.println("break!");
                                    cyklus = true;
                                    break;
                                } else {
                                    yyy = xxx;
                                    xxx = circleHasPoint(edges1.get(i).l, edges1.get(i).r, point_cloud1.get(xxx));
                                    System.out.println("navstivil som " + yyy + ", novy je " + xxx + " " + j);
                                }
                            }
//                            invalid.add(yyy);
                            //vsetky odtialto su na kruznici
                            System.out.println("°°°°" + invalid.indexOf(xxx) + "index" + xxx + " _ "+invalid.toString());
                            
                            if (cyklus) {
                                //vyries kruznicove body 
                                
                                //najdi najblizsi bod "K" ku lavemu z "kruznice" 
                                dist_last = Double.MAX_VALUE;
                                for (int k = invalid.indexOf(xxx); k < invalid.size(); k++) {
//                                    int f = (Integer)invalid.get(k);
//                                    System.out.println(f);
                                    dist = distance(edges1.get(i).l, point_cloud1.get((Integer)invalid.get(k)));
                                    if (0 >= dist.compareTo(dist_last)) {
                                        dist_last = dist;
                                        point3 = (Integer)invalid.get(k);
                                    }
                                }
                                
                                if (edgeExist(edges1.get(i).l, point_cloud1.get(point3)) && edgeExist(edges1.get(i).r, point_cloud1.get(point3))) {
                                    if (!edgeExist(edges1.get(i).l, point_cloud1.get(point3+1)) && !edgeExist(edges1.get(i).r, point_cloud1.get(point3+1))) {
                                        makeEdge(edgeID++, point_cloud1.get(point3), point_cloud1.get(point3+1));
                                        makeEdge(edgeID++, edges1.get(i).r,            point_cloud1.get(point3+1));
                                        point_cloud1.get(point3+1).setUsed();
                                        break;
                                    }
                                }else{
                                    if (edgeExist(edges1.get(i).l, point_cloud1.get(point3+1)) && edgeExist(edges1.get(i).r, point_cloud1.get(point3+1))) {
                                        if (!edgeExist(edges1.get(i).l, point_cloud1.get(point3)) && !edgeExist(edges1.get(i).r, point_cloud1.get(point3))) {
                                            makeEdge(edgeID++, point_cloud1.get(point3+1), point_cloud1.get(point3));
                                            makeEdge(edgeID++, edges1.get(i).l,            point_cloud1.get(point3));
                                            point_cloud1.get(point3).setUsed();
                                            break;
                                        }
                                    }
                                }
                            }        
//---------<<  */
   
                        }else{
                            //buď dva a skončim, alebo jeden a skončim.
                            if (!edgeExist(edges1.get(i).l, point_cloud1.get(j)) && !edgeExist(edges1.get(i).r, point_cloud1.get(j))) {
                                makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(j));
                                makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(j));
                                point_cloud1.get(j).setUsed();
                                koniec = true;
                                break; // skoncim for j
                            } else {
                                if (!edgeExist(edges1.get(i).l, point_cloud1.get(j))) {
                                    makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(j));
                                    point_cloud1.get(j).setUsed();
                                    koniec = true;
                                    break; // skoncim for j
                                } else {
                                    if (!edgeExist(edges1.get(i).r, point_cloud1.get(j))) {
                                        makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(j));
                                        point_cloud1.get(j).setUsed();
                                        koniec = true;
                                        break; // skoncim for j
                                    }
                                }
                            }
                        }
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
//                        if (-1 == xxx) {
//                            //toto teoreticky max len dvakrat zbehne, pre kazdy bod prave len jeden krat. bod je len na lavej strane  hrany, alebo na pravej
//                            //kontrola ci existuje ten 3uholnik
////                            if (!edgeExist(edges1.get(i).l, point_cloud1.get(j))) {
////                                makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(j));
////                            }
////                            if (!edgeExist(edges1.get(i).r, point_cloud1.get(j))) {
////                                makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(j));
////                            }
//                            
//                            //buď dva a skončim, alebo jeden a skončim.
//                            if (!edgeExist(edges1.get(i).l, point_cloud1.get(j)) && !edgeExist(edges1.get(i).r, point_cloud1.get(j))) {
//                                makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(j));
//                                makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(j));
//                                point_cloud1.get(j).setUsed();
//                                koniec = true;
//                                break; // skoncim for j
//                            }else{
//                                if (!edgeExist(edges1.get(i).l, point_cloud1.get(j))) {
//                                    makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(j));
//                                    point_cloud1.get(j).setUsed();
//                                    koniec = true;
//                                    break; // skoncim for j
//                                }else{
//                                    if (!edgeExist(edges1.get(i).r, point_cloud1.get(j))) {
//                                        makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(j));
//                                        point_cloud1.get(j).setUsed();
//                                        koniec = true;
//                                        break; // skoncim for j
//                                    }
//                                }
//                            }
//                        }
                        
                    }
                }
            }

//            if (koniec == true) {
//                continue; // pokracujem for pre i+1
//            }
            
            //4 not used points only
            for (int j = 0; j < point_cloud1.size(); j++) {
                if (!point_cloud1.get(j).isUsed()) {
                    if (edges1.get(i).l == point_cloud1.get(j) || edges1.get(i).r == point_cloud1.get(j)) {
                    } else {
                        
                        xxx = circleHasPoint(edges1.get(i).l, edges1.get(i).r, point_cloud1.get(j)); //tu by sa dalo opytat stale ci je v kruznici neaeky nepouzity bod a s nim pokracovat
                        if (-1 == xxx) {
//                         //make edges!!!
//                            System.out.println(edgeID + "make edges!!!");
                            makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(j));
                            makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(j));
                            point_cloud1.get(j).setUsed();
                            ui.jProgressBar1.setValue(100 * loading++ / amount);
//                        koniec = false;
                            break;
                        }
                    }
                }
            }
//            System.out.println("next edge" + i);
//            if (koniec == true) {
//                break;
//            }
        }
    }

    

    /**
     * distance from A to B
     * return double
     */
    public double distance(Point a, Point b) {
        double dx, dy;

        dx = a.getX() - b.getX();
        dy = a.getY() - b.getY();
        //System.out.println((double)Math.sqrt((double)(dx * dx + dy * dy)));
        return (double) Math.sqrt((double) (dx * dx + dy * dy));
    }

    /**
     * distance from EDGE
     */
    public double distanceFromEdge(Edge e, Point b) {
        double dx, dy;
        double x, y;

        x = (e.l.getX() + e.r.getX()) / 2;
        y = (e.l.getY() + e.r.getY()) / 2;

        dx = x - b.getX();
        dy = y - b.getY();
        //System.out.println((double)Math.sqrt((double)(dx * dx + dy * dy)));
        return (double) Math.sqrt((double) (dx * dx + dy * dy));
    }


    /**
     * sprav hranu !
     * @param i   - ID hrany
     * @param left - lavy bod hrany
     * @param right  - pravy bod hrany
     */
    private void makeEdge(int i, Point left, Point right) {
        //        edges[i] = new Edge(left, right);
        edges1.add(new Edge(left, right)); 

        double x, y;
        x = (left.getX() + right.getX()) / 2;
        y = (left.getY() + right.getY()) / 2;
        Point s = new Point((int) x, (int) y);
//        edges[i].midpoint(s);
        edges1.get(i).midpoint(s);
    }

    /**
     * delaunay circumcircle,
     * input 3 body ,
     * return cislo poradia bodu vo vnutri zadanej opisanej kruznice (dalsi (nejaky) kandidat, ak existuje)
     * inak vrati -1
     */
    private int circleHasPoint(Point a, Point b, Point c) {
        // code DELAUNAY circumcircle right here !!!
        Circle cc = circumcircle(a, b, c);
               
        for (int i = 0; i < point_cloud1.size(); i++) {
//            if (i == a.getID() || i == b.getID() || i == c.getID() ) {} //do nothing
            if (point_cloud1.get(i).getID() == a.getID() || point_cloud1.get(i).getID() == b.getID() || point_cloud1.get(i).getID() == c.getID() ) {}
            else {
                if (cc.isInside(point_cloud1.get(i))) {
                    return i;
                }
            }
        } 
        circlesA.add(cc); //adding value to ArrayList
//      cc.setvalid();
        return - 1; //ked nie je vo vnutri 3uholnika dalsi bod
    }    
    
    /**
     * Compute the circle defined by three points (circumcircle).
     */
    private Circle circumcircle(Point p1,Point p2,Point p3) {
	double stred;
        double circleX, circleY;
        circleX = circleY = 0;

	stred = crossProduct(p1, p2, p3); //TODO: zvysit presnost doubleDouble alebo nejake osetrit 4-uholniky (viac bodov na jednej kruznici)
	if (stred != 0.0)
	    {
                double p1Sq, p2Sq, p3Sq;
                double num, den;

		p1Sq = p1.getX() * p1.getX() + p1.getY() * p1.getY();
		p2Sq = p2.getX() * p2.getX() + p2.getY() * p2.getY();
		p3Sq = p3.getX() * p3.getX() + p3.getY() * p3.getY();

		num = p1Sq*(p2.getY() - p3.getY()) + p2Sq*(p3.getY() - p1.getY()) + p3Sq*(p1.getY() - p2.getY());
		circleX = num / (2.0f * stred);
		num = p1Sq*(p3.getX() - p2.getX()) + p2Sq*(p1.getX() - p3.getX()) + p3Sq*(p2.getX() - p1.getX());
		circleY = num / (2.0f * stred);
        
//                stred = distance(new Point( (int)circleX, (int)circleY), p1);
                stred = distance(new Point( circleX, circleY), p1);
	    }

	// Radius
	//r = c.distance(p1);
//        circlesA.add(new Circle(premenna,circleX,circleY)); //adding value to ArrayList
//        System.out.print("ň");
        return new Circle(stred,circleX,circleY);
    
    }

    static double crossProduct(Point p1, Point p2, Point p3) {
	double u1, v1, u2, v2;

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
    private int getOptimalStartPoint(boolean parameter, ArrayList pc, int amount) {
        metrika m = new metrika(pc, amount);
//daj mi optimalny bod = pozor! on iba vypocita MIN vzdialenost a AVG vzdialenost medzi bodmi a vrati 1.BOD
        int i = m.getPoint();
//zorad pole podla ... toho co je v      public int compareTo(Object obj) {
        if (parameter) {
System.out.println("befor sort"+point_cloud1.toString());
            point_cloud1 = m.sort();
System.out.println("after sort"+point_cloud1.toString());
            return 0;  //najvyhodnejsi je navrchu, preto netreba nam uz pouzit ret i;
        }
//        for (int t = 0; t < point_cloud.length; t++) {
//            System.out.println(">>"+point_cloud[t].getID()+">>"+point_cloud[t].getMin());
//        }
        return i;
    }

    /**
 * zaokruhlenie na pozadovany pocet des miest
 * @param Rval  - jake cislo
 * @param Rpl   - na kolko des. miest
 * @return
 */
    private Double round(Double Rval, int Rpl) {
        double p =  Math.pow(10, Rpl);
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return  tmp / p;
    }
    
    /**
     * potvrdzuje existenciu hrany medzi zadanymi bodmi
     * @param x
     * @param y
     * @return 
     */
    private boolean edgeExist(Point x, Point y) {
//        throw new UnsupportedOperationException("Not yet implemented");
//        edges1.contains(new Edge(x, y));
        
        for (int i = 0; i < edges1.size(); i++) {
//            if (((edges1.get(i).l).equals(x) && (edges1.get(i).r).equals(y)) || 
//                    ((edges1.get(i).l).equals(y) && (edges1.get(i).r).equals(x))) 
//            {
////                System.out.println("<<< Lallalala >>>");
//                return true;
//            }
            if ( 
                    ((edges1.get(i).l.getX() == x.getX()) &&  (edges1.get(i).l.getY() == x.getY()) &&
                    (edges1.get(i).r.getX() == y.getX()) &&  (edges1.get(i).r.getY() == y.getY())) ||
                    ((edges1.get(i).r.getX() == x.getX()) &&  (edges1.get(i).r.getY() == x.getY()) &&
                    (edges1.get(i).l.getX() == y.getX()) &&  (edges1.get(i).l.getY() == y.getY()))
                    ) {
//                System.out.println("<<< Lallalala >>>");
                return true;
            }
            
            
        }
        return false;
    }

    /**
     * spojenie velmi blizkych bodov v ramci zadanej tolerancie
     * @param tol 
     */
    private ArrayList collapse(Double tol) {
        Double dis;
        ArrayList<Point> ppc = new ArrayList<Point>(); 
        Point[] ppp = new Point[point_cloud1.size()];
//        Double tol = 0.001;

        System.out.println("a>>>>" + point_cloud1.size() + point_cloud1.toString());
//            for (int i = 0; i < point_cloud1.size(); i++) {
//                for (int j = 0; j < point_cloud1.size(); j++) {
//                    if (i != j) {
//                        dis = distance(point_cloud1.get(i), point_cloud1.get(j));
//                        if ( 0 <= dis.compareTo(tol) ) {
//                            point_cloud1.get(i).setX((point_cloud1.get(i).getX()+point_cloud1.get(j).getX())/2);
//                            point_cloud1.get(i).setY((point_cloud1.get(i).getY()+point_cloud1.get(j).getY())/2);
//                            point_cloud1.remove(j);
////                            point_cloud1.add(new Point( ,(point_cloud[i].getX()+point_cloud[j].getX())/2,(point_cloud[i].getY()+point_cloud[j].getY())/2));
//                        }
//                    }
//                }
//            }
        
        
        for (int i = 0; i < point_cloud1.size(); i++) {
            ppp[i] = point_cloud1.get(i);
        }
        
//            int k = 0;
        for (int i = 0; i < ppp.length; i++) {
            for (int j = 0; j < ppp.length; j++) {
                ui.jProgressBar2.setValue(100*i/ppp.length);
                if (i != j && ppp[i] != null && ppp[j] != null) {
                    dis = distance(ppp[i], ppp[j]);
//                    System.out.println("distance " + dis + ", tolerance " + tol);
//                    System.out.println(dis.compareTo(tol));
                    if (0 >= dis.compareTo(tol)) {
//                        System.out.println("spajam!");
//                            point_cloud1.add(new Point( k++, ((point_cloud[i].getX()+point_cloud[j].getX())/2), ((point_cloud[i].getY()+point_cloud[j].getY())/2)));
//                            point_cloud[i].setX((point_cloud[i].getX()+point_cloud[j].getX())/2);
//                            point_cloud[i].setY((point_cloud[i].getY()+point_cloud[j].getY())/2);
                        ppp[i] = new Point(i, round((ppp[i].getX() + ppp[j].getX()) / 2,10), round((ppp[i].getY() + ppp[j].getY()) / 2,4));
                        ppp[j] = null;
                    } else {
//                            point_cloud1.add(point_cloud[i]);
//                            point_cloud1.add(point_cloud[j]);
                    }
                }
            }
        }
        
        
        
//        System.out.println("2"+point_cloud1.toString());
        
        for (int i = 0; i < ppp.length; i++) {
            if (ppp[i] != null) {
                ppc.add(ppp[i]);
//                System.out.println(ppp[i].toString());
            }
        }
        System.out.println("b>>>>" + ppc.size() + ppc.toString());
        
        return ppc;
    }

}



