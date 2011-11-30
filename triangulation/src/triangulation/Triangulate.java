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
    public boolean startRandom = true; //nahodny start bod, false= optimalny start bod
    public int sort = 0;  //sort podla metriky, 1-ano, 0-nie-nesortuj nic
//-----<<  interaface
    
    public Point[] point_cloud ;//= new Point[amount];  //array of points2d  // ID, X, Y, used(bool)
///*zle*/    public Edge[] edges ;//= new Edge[30 * amount - 3];  //array of edges, hran je len tolko kolko je teoreticky nekonvexnych hran //akbyze potrebujeme konvexne hrany rozmyslat -> je potrebne zvysit kapacitu pola, o neviem kolko :) (pripadne vyrobit druhe pole urcene len an tieto hrany)
    public Edge[] convex = new Edge[100];  //array of edges, hrany konvexne
/*zle*/    //public Edge[] edges = new Edge[100];  //array of edges, hran je len tolko kolko je teoreticky nekonvexnych hran //akbyze potrebujeme konvexne hrany rozmyslat -> je potrebne zvysit kapacitu pola, o neviem kolko :) (pripadne vyrobit druhy LIST urceny len na tieto hrany)
    public Circle[] circles ;//= new Circle[amount];
    ArrayList<Circle> circlesA = new ArrayList<Circle>();
    ArrayList<Edge> convexA = new ArrayList<Edge>();
    ArrayList<Edge> edges1 = new ArrayList<Edge>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //System.out.println((int)(Math.random() * Math.random() * 100));
        System.out.println("////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("Triangulation algorithm of Boris Nikolaevich Delaunay, code by Dominik Januvka. 2011");
        System.out.println("Version 2.9 , NEWS: new 3angulation method");
        System.out.println("////////////////////////////////////////////////////////////////////////////////////");
        // volanie spustenia aplikacie
        Triangulate triangulate = new Triangulate();

    }
    
    /**
     * samotne spustenie
     */
    public Triangulate() {
        //spustime user interface pre nastavenie 
        UIfc ui = new UIfc(null, true);
        ui.setVisible(true);
        
        amount = ui.getAmount();
        startRandom = ui.isStartRandom();
        sort = ui.getSort();
        
point_cloud = new Point[amount];
//edges = new Edge[30 * amount - 3];
circles = new Circle[amount];
        
        //neake nahodne mracno bodov v 2d
        getRandomPoints(amount);  
//        for (int i = 0; i < point_cloud.length; i++) {
//            System.out.println(point_cloud[i].toString()); //show points in point_cloud
//        }
        
        int startPointID = 0;  //startovaci bod
        if (startRandom) {
            // zvolime si nahodne startovaci bod  ---alebo
            startPointID = getRandomStartPoint(amount);
            System.out.println("first point: " + point_cloud[startPointID]);
        } else {
            //--- alebo... zvolime si optimalny bod podla metriky
            //parameter 1-ked chcem sortovat pole pointov,0-nechcem nic sortovat
            startPointID = getOptimalStartPoint(sort, point_cloud, amount);
            System.out.println("first OPTIMAL point: " + point_cloud[startPointID]);
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
        gui.Kresli(point_cloud, edges1, circlesA);
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
//        point_cloud[2] = new Point(2,10,30);
//        point_cloud[3] = new Point(3,20,10);
//        point_cloud[4] = new Point(4,20,20);
//        point_cloud[5] = new Point(5,20,30);
//        point_cloud[6] = new Point(6,30,10);
//        point_cloud[7] = new Point(7,30,20);
//        point_cloud[8] = new Point(8,30,30);
//        point_cloud[9] = new Point(9,40,10);

        for (int i = 0; i < point_cloud.length; i++) {
            point_cloud[i] = new Point(i, /*(int)*/ round((Math.random() * Math.random() * 100),4), /*(int)*/ round((Math.random() * Math.random() * 100),4));
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
        point_cloud[startPointID].setUsed(); //nastavenia priznaku "zneuzitia" tohto bodu musi byt TU na zaciatku aby sme ho uz pri FORe nepouzili
        Double dist = new Double(0);
        Double dist_last = Double.MAX_VALUE;
        int point2 = 0;
        int point3 = 0;
        int counter = 0;  //univerzalne pocitadlo, mozem recyklovat podla potreby !

        
//----hladanie 2. bodu a 1. hrany---------
        for (int i = 0; i < point_cloud.length; i++) { //porovname vsetky vzdialenosti bodov, prveho vybrateho so vsetkymi, a tak najdeme 2. bod trojuholnika
            if (!(point_cloud[i].isUsed())) {     //vsetky okrem prveho vybrateho //je pouzity? ak NIE pokracuj alg, Ak ANO
                dist = distance(point_cloud[startPointID], point_cloud[i]);
                if (0 >= dist.compareTo(dist_last)) {
                    dist_last = dist;
                    point2 = i;
                }
            }
        }
        System.out.println("second point. distance: " + dist_last + ", point: " + point_cloud[point2].toString());
        makeEdge(0, point_cloud[startPointID], point_cloud[point2]);
        point_cloud[point2].setUsed();
        //edges[0].setUsed();
        
//-----hladanie 3. bodu a 2.+3. hrany------------------------------------
        dist_last = Double.MAX_VALUE;

        for (int i = 0; i < point_cloud.length; i++) {
            if (!(point_cloud[i].isUsed())) {     //vsetky okrem prveho vybrateho
                //porovname vsetky vzdialenosti bodov od 2. bodu, MOZME hladat aj od STREDU KRUZNICE (ak chceme)!!!
                //TODO: MOZME zobrat hned PRVY nepouzity z hora lebo ak su optimalne sortovane netreba hladat najblizsi!!!
//                dist = distanceFromEdge(edges[0], point_cloud[i]);
                dist = distanceFromEdge(edges1.get(0), point_cloud[i]);
                //if (dist < dist_last) {  // XXX pouzit ine porovnavanie
                if (0 >= dist.compareTo(dist_last)) {  
                    dist_last = dist;
                    point3 = i;
                }
            }
        }
        System.out.println("third point. distance from edge: " + dist_last + ", point: " + point_cloud[point3].toString());
        
        //Alg na hladanie vhodnejsieho kandidata!
        int xxx = circleHasPoint(point_cloud[startPointID], point_cloud[point2], point_cloud[point3]);
        if (xxx != -1) {
            int yyy = -1;
            ArrayList invalid = new ArrayList(); // declares an array of integers        
            
            while (xxx != -1) {
                if (yyy == -1) {
                    invalid.add(point3);
                } else {
                    invalid.add(yyy);   //nastane len raz.
                }
                if (invalid.contains(xxx)) {
                    point3 = yyy;
                    System.out.println("break!");
                    break;
                } else {
                    yyy = xxx;
                    xxx = circleHasPoint(point_cloud[startPointID], point_cloud[point2], point_cloud[xxx]);
                    System.out.println("navstivil som " + yyy + ", novy je " + xxx);
                }
            }

            if (xxx == -1) {   // ak sme nasli nieco vhodnejsie tak to dame do POINT3
                point3 = yyy;
                System.out.println("oprava");
            }
        }
        
        System.out.println("----------koniec prveho 3uholnika---------------");
        
        
        makeEdge(1, point_cloud[startPointID], point_cloud[point3]);
        makeEdge(2, point_cloud[point2], point_cloud[point3]);
        point_cloud[point3].setUsed();
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
        
        
//        for (int f = 0; f < point_cloud.length; f++) {
//            point_cloud[f].setUsed();
//        }
        
        for (int i = 0; i < edges1.size(); i++) {

            //4 used edges only
            for (int j = 0; j < point_cloud.length; j++) {
                if (point_cloud[j].isUsed()) {
                    if (edges1.get(i).l == point_cloud[j] || edges1.get(i).r == point_cloud[j]) {
                    } else {
                        xxx = circleHasPoint(edges1.get(i).l, edges1.get(i).r, point_cloud[j]);
                        if (-1 == xxx) {
                            //toto teoreticky len dvakrat zbehne, lebo bod je len na lavej strane  hrany, alebo na pravej
                            //ked nastane druhykrat -1jednotka, uz sa nemusi cyklus opakovat lebo aj tak uz nenastane ten pripad co chceme
                            //kontrola ci existuje ten 3uholnik
                            if (!edgeExist(edges1.get(i).l, point_cloud[j])) {
                                makeEdge(edgeID++, edges1.get(i).l, point_cloud[j]);
                            }
                            if (!edgeExist(edges1.get(i).r, point_cloud[j])) {
                                makeEdge(edgeID++, edges1.get(i).r, point_cloud[j]);
                            }
                        }
                    }
                }
            }

            //4 not used edges only
            for (int j = 0; j < point_cloud.length; j++) {
                if (!point_cloud[j].isUsed()) {
                    if ( -1 == circleHasPoint(edges1.get(i).l, edges1.get(i).r, point_cloud[j])) { //tu by sa dalo opytat stale ci je v kruznici neaeky nepouzity bod a s nim pokracovat
                        //make edges!!!
                        System.out.println(edgeID + "make edges!!!");
                        makeEdge(edgeID++, edges1.get(i).l, point_cloud[j]);
                        makeEdge(edgeID++, edges1.get(i).r, point_cloud[j]);
                        point_cloud[j].setUsed();
//                        koniec = false;
                        break;
                    } else {
//                        koniec = true;
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
     *
     * @param i
     * @param j
     * @return y - dalsi kandidat
     */
//    private int solve(int i, int j) {
//// -->> stat =   // until stat != -1 AND !p_c(stat).IsUsed
//        int y;
//
//        if (j == -1) {
//            return j;
//        } else {
//            y = j;
//            j = circleHasPoint(edges[i].l, edges[i].r, point_cloud[j]);  //vrati bod vo vnutri opisanej kruznice tymito tromi bodmi
//            System.out.println("solve->" + j);
//            solve(i, j);
//            return y;
//        }
//    }

    /**
     * nahrada za povodny solve, rozdiel je v tom, ze ...
     * @param i
     * @param j
     * @return vrati dalsieho najlepsieho kandidata
     */
//    private int solve1(int i, int j) {
//        int bool = circleHasPoint(edges[i].l, edges[i].r, point_cloud[j]);
//        if (bool == -1) {
//            return j;
//        }
//        int[] last = new int[5];
//        int y = 0;
//        int z = 0; // aktivator kontroly zacyklenia
//
//        while (bool != -1) {   //rekurzia ci nema opisana kruznica este dalsie body v sebe
//            z++;
//            if (z >= 5) {
//                if (last[y - 1] == bool || last[y - 2] == bool || last[y - 3] == bool || last[y - 4] == bool) {
//                    break;
//                }
//            }
//
//            last[y++] = bool;
//            if (y == 4) {
//                y = 0;
//            }
//
//
//            j = bool;
//            bool = circleHasPoint(edges[i].l, edges[i].r, point_cloud[j]);
//        }
//        return j;
//    }

//    private int solve2 (){
//        Double dist_last = Double.MAX_VALUE;
//        Double dist;
//
//        for (int i = 0; i < point_cloud.length; i++) {
//            if (!(point_cloud[i].isUsed())) {     //vsetky okrem prveho vybrateho
//                //porovname vsetky vzdialenosti bodov od 2. bodu, MOZME hladat aj od STREDU KRUZNICE (ak chceme)!!!
//                //TODO: MOZME zobrat hned PEVY nepouzity z hora lebo ak su optimalne sortovane netreba hladat najblizsi!!!
//                dist = distanceFromEdge(edges[0], point_cloud[i]);
//                //if (dist < dist_last) {  // XXX pouzit ine porovnavanie
//                if (0 >= dist.compareTo(dist_last)) {  
//                    dist_last = dist;
//                    point3 = i;
//                }
//            }
//        }
//        System.out.println("third point. distance from edge: " + dist_last + ", point: " + point_cloud[point3].toString());
//        
//        int xxx = circleHasPoint(point_cloud[startPointID], point_cloud[point2], point_cloud[point3]);
//        
//        if (xxx != -1) {
//            int yyy = -1;
//            ArrayList invalid = new ArrayList(); // declares an array of integers        
//            
//            while (xxx != -1) {
//                if (yyy == -1) {
//                    invalid.add(point3);
//                } else {
//                    invalid.add(yyy);   //nastane len raz.
//                }
//                if (invalid.contains(xxx)) {
//                    point3 = yyy;
//                    System.out.println("break!");
//                    break;
//                } else {
//                    yyy = xxx;
//                    xxx = circleHasPoint(point_cloud[startPointID], point_cloud[point2], point_cloud[xxx]);
//                    System.out.println("navstivil som " + yyy + ", novy je " + xxx);
//                }
//            }
//
//            if (xxx == -1) {   // ak sme nasli nieco vhodnejsie tak to dame do POINT3
//                point3 = yyy;
//                System.out.println("oprava");
//            }
//        }
//        return 0;
//    }

    

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
        edges1.add(new Edge(left, right)); //TODO: overit ci je mozne aby nebolo potrebne edgeID, a stacilo len poradove cislo v liste

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
     * return je dalsi (nejaky) kandidat, ak existuje vo vnutri opisanej kruznice.
     * inak vrati -1
     */
    private int circleHasPoint(Point a, Point b, Point c) {
        // TODO: code DELAUNAY circumcircle right here !!!
        Circle cc = circumcircle(a, b, c);
               
        for (int i = 0; i < point_cloud.length; i++) {
            if (i == a.getID() || i == b.getID() || i == c.getID() ) {} //do nothing
            else {
                if (cc.isInside(point_cloud[i])) {
                    return i;
                }
            }
        } 
        return - 1; //ked nie je vo vnutri 3uholnika dalsi bod
    }    
    
    /**
     * Compute the circle defined by three points (circumcircle).
     */
    private Circle circumcircle(Point p1,Point p2,Point p3) {
	double premenna;
        double circleX, circleY;
        circleX = circleY = 0;

	premenna = crossProduct(p1, p2, p3);
	if (premenna != 0.0)
	    {
                double p1Sq, p2Sq, p3Sq;
                double num, den;

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
        System.out.print("ň");
        return new Circle(premenna,circleX,circleY);
    
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
    private int getOptimalStartPoint(int parameter, Point[] pc, int amount) {
        metrika m = new metrika(pc, amount);
//daj mi optimalny bod = pozor! on iba vypocita MIN vzdialenost a AVG vzdialenost medzi bodmi a vrati 1.BOD
        int i = m.getPoint();
//zorad pole podla ... toho co je v      public int compareTo(Object obj) {
        if (parameter==1) {
            point_cloud = m.sort();
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
//                System.out.println("<<< Lallalala >>>");
//                return true;
//            }
            if ( 
                    ((edges1.get(i).l.getX() == x.getX()) &&  (edges1.get(i).l.getY() == x.getY()) &&
                    (edges1.get(i).r.getX() == y.getX()) &&  (edges1.get(i).r.getY() == y.getY())) ||
                    ((edges1.get(i).r.getX() == x.getX()) &&  (edges1.get(i).r.getY() == x.getY()) &&
                    (edges1.get(i).l.getX() == y.getX()) &&  (edges1.get(i).l.getY() == y.getY()))
                    ) {
                System.out.println("<<< Lallalala >>>");
                return true;
            }
            
            
        }
        return false;
    }

}



















//
//        // edge startujeme pocitat od 3
//        Double dist = new Double(0);
//        Double dist_last = Double.MAX_VALUE;
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
//            dist_last = Double.MAX_VALUE;
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
    