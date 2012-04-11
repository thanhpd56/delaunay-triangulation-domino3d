/*
 * Hlavna trieda triangulacie spustajuca vsetky metody.
 */
package triangulation;

//import com.sun.org.apache.bcel.internal.generic.RETURN;
//import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
//import java.applet.*;
//import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Math;
//import java.io.PrintStream;
//import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
//import triangulation.Point;
import java.util.Random;




/**
 * * Hlavna trieda triangulacie spustajuca vsetky metody.
 * @author Dominik Januvka 2011
 */
public class Triangulate {
    
//----->>  interaface
    private int amount = 20;        //kolko bodov chcem/mam v poli
    private boolean startRandom ;   //= true; //nahodny start bod, false= optimalny start bod
    private boolean collapse ;      //= true; //nahodny start bod, false= optimalny start bod
    private boolean sort ;          //sort podla metriky, 1-ano, 0-nie-nesortuj nic
    private UIfc ui;
    private int loading = 1;
//-----<<  interaface
    
    ArrayList<Circle> circlesA = new ArrayList<Circle>();
    ArrayList<Edge> convexA = new ArrayList<Edge>();
    ArrayList<Edge> edges1 = new ArrayList<Edge>();
    ArrayList<Point> point_cloud1 = new ArrayList<Point>();  //array of points2d  // ID, X, Y, used(bool)
    ArrayList<Face> face = new ArrayList<Face>();           //array of faces
    private int kon1 = 12;  //konstanta 1 pri vybere vhodnosti 3uh.
    private int kon2 = 5;   //konstanta 2 pri maximalnej vzdialenosti
    private boolean fromFile;
    private String file;
//    private int firstTriangle[] = new int[3];
    private int firstTriangle[] = new int[9];

//---------- štatistiky
    private long timeGpu = -1;
    private long timeCpu = -1;
    private long timeGpuMetrika = -1;
    private long timeCpuMetrika = -1;
    private long timeGpuCollapse = -1;
    private long timeCpuCollapse = -1;
    private double tolerancia = -1;
    private String deviceName = null;
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //System.out.println((int)(Math.random() * Math.random() * 100));
        System.out.println("////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("Triangulation algorithm of Boris Nikolaevich Delaunay, code by Dominik Januvka. 2011/2012");
        System.out.println("Version 3 , NEWS: 3D");
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
        if(ui.isExit())System.exit(1); //exit on exit
        
        amount = ui.getAmount();
        startRandom = ui.isStartRandom();
        sort = ui.getSort();
        collapse = ui.isCollapse();
        kon1 = ui.getKon1();
        kon2 = ui.getKon2();
        fromFile = ui.isFromFile();
        file = ui.getFile();
        if (fromFile && file == null) {
            System.out.println("Nebol vybraty ziaden subor! Koncim");
            System.exit(0); //exit on exit
        }
                
        if (amount >= 9 ) {
        } else {
            System.out.println("nedostatocny pocet bodov pre triangulaciu. KONCIM!");
            System.exit(0);
            return ;
        }
        
        ui.setStatus2("Načítavanie bodov");
        //neake nahodne mracno bodov v 2d
//        getRandomPoints(amount);
        amount=getRandomPoints(amount, fromFile);

        ui.setStatus2("Spájam blízke body");
        //blizke body spojim podla parametru    (Double) ui.getTolerance().getText()
        if (collapse) {
            if (ui.isCol()) {
                point_cloud1 = CLcollapse(Double.parseDouble(ui.getTolerance().getText()));
            } else {
                point_cloud1 = collapse(Double.parseDouble(ui.getTolerance().getText()));
            }
            tolerancia = Double.parseDouble(ui.getTolerance().getText());
//            System.out.println("colapse tolerance " + Double.parseDouble(ui.getTolerance().getText()));
            System.out.println("colapse >> amount " + point_cloud1.size() );
            amount = point_cloud1.size();
        } else {
        }

        

        ui.setStatus2("Načítavanie metriky bodov");
        int startPointID = 0;  //startovaci bod
        if (startRandom) {
            // zvolime si nahodne startovaci bod  ---alebo
            startPointID = getRandomStartPoint(amount);
            if (ui.isMet()) {
                CLgetOptimalStartPoint(sort, point_cloud1, amount);
            } else {
                getOptimalStartPoint(sort, point_cloud1, amount);
            }
            System.out.println("first RND point: " + point_cloud1.get(startPointID));
        } else {
            //--- alebo... zvolime si optimalny bod podla metriky
            //parameter 1-ked chcem sortovat pole pointov,0-nechcem nic sortovat
            if (ui.isMet()) {
                startPointID = CLgetOptimalStartPoint(sort, point_cloud1, amount);
            } else {
                startPointID = getOptimalStartPoint(sort, point_cloud1, amount);
            }
            System.out.println("first OPTIMAL point: " + point_cloud1.get(startPointID));
        }

        
        ui.setStatus2("Tvorba prvého trojuholníka");
        // spravime prvye 3 trojuholniky!!!
        if (amount >= 9 ) {
            firstTriangle(startPointID);
        } else {
            System.out.println("nedostatocny pocet bodov pre triangulaciu. KONCIM!");
            System.exit(0);
            return ;  // ???
        }
        
        
        ui.setStatus2("Tvorba povrchu ");
        /* teraz treba hladat najblizsi bod k trojuhoniku
        a - porovnat vzdialenost kazdej hrany s najblizsim najdenym vybranym bodom   //*** optimalizacia
        b - circumcircles
        c - spirala, pozerat +1 bod hore, dole, doprava...
         */
        if (ui.isTri()) {
            CLtriangulate();  //ostatne 3uholniky cez OenCL 
        } else {
            long start = System.currentTimeMillis();
            triangulate();  //ostatne 3uholniky
            long end = System.currentTimeMillis();
            timeCpu = end - start;
        }
        
        
        
        
//        //otvorime si okienko a nakreslim co to vlastne vyrobilo
//        Show gui = new Show();
//        gui.setVisible(true);
//        gui.Kresli(point_cloud1, edges1, circlesA);
        
        //export do wavefront OBJ
        if (ui.isOBJ()) exportOBJ();
        //export do wavefront WRL
        if (ui.isWRL()) exportWRL();
        //export do wavefront ASE
        if (ui.isASE()) exportASE();
        
        //statistika
        stat();
        
        //vypocet uplnosti povrchu modelu
        System.out.println("Úplnosť povrchu modelu: "+ui.jProgressBar1.getValue()+" %");
        ui.setStatus1("Úplnosť povrchu modelu: "+ui.jProgressBar1.getValue()+" %");
        if (ui.jProgressBar1.getValue() < 70) {
            System.out.println("Úplnosť modelu je pod 70%, doporučujem spustiť trianguláciu s inými nastaveniami.");
            ui.setStatus1("Úplnosť modelu je pod 70% (t.j."+ ui.jProgressBar1.getValue() +"%), doporučujem spustiť trianguláciu s inými nastaveniami.");
        }
    }

    /**
     * Generates a set of random points to triangulate, nums from 0.0 to 100.0.
     */
//    private void getRandomPoints(int amount) {
    private int getRandomPoints(int amount, boolean fromFile) {
        if (!fromFile) {
            for (int i = 0; i < amount; i++) {
                point_cloud1.add(new Point(i, (Math.random() * Math.random() * 100), (Math.random() * Math.random() * 100), (Math.random() * Math.random() * 100)));
//                point_cloud1.add(new Point(i, (Math.random() * Math.random() * 100), (Math.random() * Math.random() * 100), 1));
//todo: generovanie nahod v 2D !!!
                
                
//        point_cloud1.add( new Point(0,10,10,1));
//        point_cloud1.add( new Point(1,10,20,1));
//        point_cloud1.add( new Point(2,10,30,1));
//        point_cloud1.add( new Point(3,20,10,1));
//        point_cloud1.add( new Point(4,20,20,1));
//        point_cloud1.add( new Point(5,20,30,1));
//        point_cloud1.add( new Point(6,30,10,1));
//        point_cloud1.add( new Point(7,30,20,1));
//        point_cloud1.add( new Point(8,30,30,1));
//        point_cloud1.add( new Point(9,40,10,1));

            }
            return amount;
        } else {
            ArrayList<String[]> riadok = new ArrayList<String[]>();
            String[] tokens;
            try {
                // Open the file that is the first 
                // command line parameter
                FileInputStream fstream = new FileInputStream(""+file);
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                //Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    String delims = "[ ]+";
                    tokens = strLine.split(delims);
                    riadok.add(tokens);
                }
                //Close the input stream
                in.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

            int pc = 0;
            for (int i = 0; i < riadok.size(); i++) {
                tokens = riadok.get(i);
                if ("v".equals(tokens[0])) {
                    point_cloud1.add(new Point(pc++, Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])));
                }
            }
            return pc;
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
//        int counter = 0;  //univerzalne pocitadlo, mozem recyklovat podla potreby !

        
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
        System.out.println("second point " + point_cloud1.get(point2).toString());
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
        System.out.println("third point " + point_cloud1.get(point3).toString());
        
        //Alg na hladanie vhodnejsieho kandidata!
        int xxx = circleHasPoint1(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(point3));
        if (xxx != -1) {
            int yyy = -1;
            ArrayList invalid = new ArrayList(); // declares an array of integers        
            
            while (xxx != -1) {
                if (xxx == -2) xxx = getRandomStartPoint(amount);
                if (yyy == -1) {
                    invalid.add(point3); //nastane len raz.
                } else {
                    invalid.add(yyy);   
                }
                if (invalid.contains(xxx) ) {
                    point3 = yyy;
                    System.out.println("break!");
                    break;
                } else {
                    yyy = xxx;
                    xxx = circleHasPoint1(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(xxx));
                    System.out.println("navstivil som " + yyy + ", novy je " + xxx);
                }
            }

            
            if (xxx == -1 ) {   // ak sme nasli nieco vhodnejsie tak to dame do POINT3
                point3 = yyy;
                System.out.println("oprava");
            }
        }
        
        System.out.println("----------koniec prveho 3uholnika---------------");
        
        
        makeEdge(1, point_cloud1.get(startPointID), point_cloud1.get(point3));
        makeEdge(2, point_cloud1.get(point2), point_cloud1.get(point3));
//        face.add(new Face(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(point3)));
        face.add(new Face(startPointID, point2, point3));
        firstTriangle[0] = startPointID;
        firstTriangle[1] = point2;
        firstTriangle[2] = point3;
        point_cloud1.get(point3).setUsed();
        ui.jProgressBar1.setValue(100*loading++/amount);
        
//------dalsie dva trrojuholniky-----------------------------
//-----------------------------------------------------------
//-----------------------------------------------------------
        
        
        ui.jProgressBar1.setValue(100*loading++/amount);
        dist = 0.0;
        dist_last = Double.MAX_VALUE;
        point2 = 0;
        point3 = 0;
        
//        startPointID = getRandomStartPoint(amount);
        startPointID = (int)(amount/2);
        while( firstTriangle[0] == startPointID ||
            firstTriangle[1] == startPointID ||
            firstTriangle[2] == startPointID  ){
            startPointID = getRandomStartPoint(amount);
        }
        point_cloud1.get(startPointID).setUsed();
        System.out.println("x first point " + point_cloud1.get(startPointID).toString());
        
        
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
        System.out.println("x second point " + point_cloud1.get(point2).toString());
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
        System.out.println("x third point " + point_cloud1.get(point3).toString());
        
        //Alg na hladanie vhodnejsieho kandidata!
        xxx = circleHasPoint1(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(point3));
        if (xxx != -1) {
            int yyy = -1;
            ArrayList invalid = new ArrayList(); // declares an array of integers        
            
            while (xxx != -1) {
                if (xxx == -2) xxx = getRandomStartPoint(amount);
                if (yyy == -1) {
                    invalid.add(point3); //nastane len raz.
                } else {
                    invalid.add(yyy);   
                }
                if (invalid.contains(xxx) ) {
                    point3 = yyy;
                    System.out.println("break!");
                    break;
                } else {
                    yyy = xxx;
                    xxx = circleHasPoint1(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(xxx));
                    System.out.println("x navstivil som " + yyy + ", novy je " + xxx);
                }
            }

            
            if (xxx == -1 ) {   // ak sme nasli nieco vhodnejsie tak to dame do POINT3
                point3 = yyy;
                System.out.println("x oprava");
            }
        }
        
        System.out.println("x ----------koniec prveho 3uholnika---------------");
        
        
        makeEdge(1, point_cloud1.get(startPointID), point_cloud1.get(point3));
        makeEdge(2, point_cloud1.get(point2), point_cloud1.get(point3));
//        face.add(new Face(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(point3)));
        face.add(new Face(startPointID, point2, point3));
        firstTriangle[3] = startPointID;
        firstTriangle[4] = point2;
        firstTriangle[5] = point3;
        point_cloud1.get(point3).setUsed();
        ui.jProgressBar1.setValue(100*loading++/amount);

        
//------ este dalsie dva trrojuholniky-----------------------------
//-----------------------------------------------------------
//-----------------------------------------------------------
        
        
        ui.jProgressBar1.setValue(100*loading++/amount);
        dist = 0.0;
        dist_last = Double.MAX_VALUE;
        point2 = 0;
        point3 = 0;
        
//        startPointID = getRandomStartPoint(amount);
        startPointID = amount-1;
        while( firstTriangle[0] == startPointID ||
            firstTriangle[1] == startPointID ||
            firstTriangle[2] == startPointID ||
            firstTriangle[3] == startPointID ||
            firstTriangle[4] == startPointID ||
            firstTriangle[5] == startPointID ){
            startPointID = getRandomStartPoint(amount);
        }
        point_cloud1.get(startPointID).setUsed();
        System.out.println("y first point " + point_cloud1.get(startPointID).toString());
        
        
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
        System.out.println("y second point " + point_cloud1.get(point2).toString());
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
        System.out.println("y third point " + point_cloud1.get(point3).toString());
        
        //Alg na hladanie vhodnejsieho kandidata!
        xxx = circleHasPoint1(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(point3));
        if (xxx != -1) {
            int yyy = -1;
            ArrayList invalid = new ArrayList(); // declares an array of integers        
            
            while (xxx != -1) {
                if (xxx == -2) xxx = getRandomStartPoint(amount);
                if (yyy == -1) {
                    invalid.add(point3); //nastane len raz.
                } else {
                    invalid.add(yyy);   
                }
                if (invalid.contains(xxx) ) {
                    point3 = yyy;
                    System.out.println("break!");
                    break;
                } else {
                    yyy = xxx;
                    xxx = circleHasPoint1(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(xxx));
                    System.out.println("y navstivil som " + yyy + ", novy je " + xxx);
                }
            }

            
            if (xxx == -1 ) {   // ak sme nasli nieco vhodnejsie tak to dame do POINT3
                point3 = yyy;
                System.out.println("y oprava");
            }
        }
        
        System.out.println("y ----------koniec prveho 3uholnika---------------");
        
        
        makeEdge(1, point_cloud1.get(startPointID), point_cloud1.get(point3));
        makeEdge(2, point_cloud1.get(point2), point_cloud1.get(point3));
//        face.add(new Face(point_cloud1.get(startPointID), point_cloud1.get(point2), point_cloud1.get(point3)));
        face.add(new Face(startPointID, point2, point3));
        firstTriangle[6] = startPointID;
        firstTriangle[7] = point2;
        firstTriangle[8] = point3;
        point_cloud1.get(point3).setUsed();
        ui.jProgressBar1.setValue(100*loading++/amount);
        
        
    }


//------------------------------------------------------------------------------
    /**
     * samotna triangulacia
     */
    private void triangulate() {
        int edgeID = 3;
        int edgeJ = -1;
        int edgeK = -1;
        int xxx;
        Double dist = new Double(0);
        Double dist_last = Double.MAX_VALUE;
        
        
        
        for (int i = 0; i < edges1.size(); i++) {
         edgeJ = -1;
         dist_last = Double.MAX_VALUE;

            //4/for used points only
            for (int j = 0; j < point_cloud1.size(); j++) {
                if (point_cloud1.get(j).isUsed()) {
                    if (edges1.get(i).l == point_cloud1.get(j) || edges1.get(i).r == point_cloud1.get(j)) { //todo: nie som si isty ci takto to mozem porovnavat
                    } else {
                        xxx = circleHasPoint(edges1.get(i).l, edges1.get(i).r, point_cloud1.get(j));
                        
                        //    kontrola ci mam najvyhodnejsi, najmensi, 3uholnik a az tak striangulovat ALEBO ci je bod na vyhovujucej(blizkej) rovine
                        //    potom zacat robit trojuholnik >>> code <<<
//                        if ( xxx == -1) {
                        if ( xxx != -2 && xxx != -3) {

//                            break;
//                            dist = Math.acos(skalarSucin(edges1.get(edgeID-1).getMidpoint(), edges1.get(i).getMidpoint(), point_cloud1.get(j)));
//                            dist = circlesA.get(circlesA.size()-1).getR();  //posledny pridany kruh neobsahujuci bod
                            dist = distanceFromEdge(edges1.get(i), point_cloud1.get(j));  
                            if (point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).l, point_cloud1.get(j)) 
                                    || point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).r, point_cloud1.get(j)) ) 
                            {
                                if (dist.compareTo(dist_last) < 0) {
//                                if (dist.compareTo(dist_last) > 0) {
                                    dist_last = dist;
                                    edgeJ = j;
//                                    System.out.println("..." + edgeJ);
                                }
                            }
                        }
                    }
                }
            }
            if (edgeJ != -1) {  //na zaciatku nebude mat 3uh. z pouzitych bodov

/*experimentalne vypnut*/    if (point_cloud1.get(edgeJ).getMin()*kon2 > distance(edges1.get(i).l, point_cloud1.get(edgeJ)) || point_cloud1.get(edgeJ).getMin()*kon2 > distance(edges1.get(i).r, point_cloud1.get(edgeJ))   ) //urcenie neakej maximalnej dlzky hrany
///*experimentalne vypnut*/    if (point_cloud1.get(edgeJ).getAvg()/2 > distance(edges1.get(i).l, point_cloud1.get(edgeJ)) || point_cloud1.get(edgeJ).getAvg()/2 > distance(edges1.get(i).r, point_cloud1.get(edgeJ)) ) //urcenie neakej maximalnej dlzky hrany
///*experimentalne vypnut*/    if (20 > distance(edges1.get(i).l, point_cloud1.get(edgeJ)) || 20 > distance(edges1.get(i).r, point_cloud1.get(edgeJ)) ) //urcenie neakej maximalnej dlzky hrany
                        {
                            //buď dve hrany a skončim, alebo jedna a skončim.
                            if (!edgeExist(edges1.get(i).l, point_cloud1.get(edgeJ)) && !edgeExist(edges1.get(i).r, point_cloud1.get(edgeJ))) {
                                makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(edgeJ));
                                makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(edgeJ));
//                                face.add(new Face( edges1.get(i).l, point_cloud1.get(edgeJ), edges1.get(i).r));
                                face.add(new Face( point_cloud1.indexOf(edges1.get(i).l), edgeJ, point_cloud1.indexOf(edges1.get(i).r) ));
                                point_cloud1.get(edgeJ).setUsed();
//                                break; // skoncim hladanie bodu for j
                            } else {
                                if (!edgeExist(edges1.get(i).l, point_cloud1.get(edgeJ))) {
                                    makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(edgeJ));
//                                    face.add(new Face( edges1.get(i).l, point_cloud1.get(edgeJ), edges1.get(i).r));
                                    face.add(new Face( point_cloud1.indexOf(edges1.get(i).l), edgeJ, point_cloud1.indexOf(edges1.get(i).r)));
                                    point_cloud1.get(edgeJ).setUsed();
//                                    break; // skoncim for j
                                } else {
                                    if (!edgeExist(edges1.get(i).r, point_cloud1.get(edgeJ))) {
                                        makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(edgeJ));
//                                        face.add(new Face( edges1.get(i).l, point_cloud1.get(edgeJ), edges1.get(i).r));
                                        face.add(new Face( point_cloud1.indexOf(edges1.get(i).l), edgeJ, point_cloud1.indexOf(edges1.get(i).r)));
                                        point_cloud1.get(edgeJ).setUsed();
//                                        break; // skoncim for j
                                    }
                                }
//                                face.add(new Face( edges1.get(i).l, point_cloud1.get(edgeJ), edges1.get(i).r));
                                face.add(new Face( point_cloud1.indexOf(edges1.get(i).l), edgeJ, point_cloud1.indexOf(edges1.get(i).r)));
                            }
                        }
            }

            
            
            
            
//----------------------------------
            edgeJ = -1;
            dist_last = Double.MAX_VALUE;
            
            //4 not used points only
            for (int j = 0; j < point_cloud1.size(); j++) {
                if (!point_cloud1.get(j).isUsed()) {
                    if (edges1.get(i).l == point_cloud1.get(j) || edges1.get(i).r == point_cloud1.get(j)) {
                    } else {
                        xxx = circleHasPoint(edges1.get(i).l, edges1.get(i).r, point_cloud1.get(j)); //tu by sa dalo opytat stale ci je v kruznici neaeky nepouzity bod a s nim pokracovat
                        
                        //    kontrola ci mam najvyhodnejsi, najmensi, 3uholnik a az tak striangulovat ALEBO ci je bod na vyhovujucej(blizkej) rovine
                        //    potom zacat robit trojuholnik >>> code <<<
                        
                        //                        if (xxx != -2 && xxx == -1) {
//                        if ( xxx == -1) {
                        if ( xxx != -2 && xxx != -3) {
                            edgeJ = j;
//                            System.out.println("XXX"+edgeJ);
//                            break;
//                            dist = Math.acos(skalarSucin(edges1.get(edgeID-1).getMidpoint(), edges1.get(i).getMidpoint(), point_cloud1.get(j)));
//                            dist = circlesA.get(circlesA.size()-1).getR();
                            dist = distanceFromEdge(edges1.get(i), point_cloud1.get(j));  
                            if (point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).l, point_cloud1.get(j)) 
                                    || point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).r, point_cloud1.get(j)) ) 
                            {
                                if (dist.compareTo(dist_last) < 0) {
//                                if (dist.compareTo(dist_last) > 0) {
                                    dist_last = dist;
                                    edgeJ = j;
//                                    System.out.println("XXX" + edgeJ);
                                }
                            }
                        }
                    }
                }
            }
            
            if (edgeJ != -1) {

/*experimentalne vypnut*/  if (point_cloud1.get(edgeJ).getMin()*kon2 > distance(edges1.get(i).l, point_cloud1.get(edgeJ)) || point_cloud1.get(edgeJ).getMin()*kon2 > distance(edges1.get(i).r, point_cloud1.get(edgeJ))     ) 
///*experimentalne vypnut*/  if (point_cloud1.get(edgeJ).getAvg()/2 > distance(edges1.get(i).l, point_cloud1.get(edgeJ)) || point_cloud1.get(edgeJ).getAvg()/2 > distance(edges1.get(i).r, point_cloud1.get(edgeJ)) ) 
///*experimentalne vypnut*/  if (20 > distance(edges1.get(i).l, point_cloud1.get(edgeJ)) || 20 > distance(edges1.get(i).r, point_cloud1.get(edgeJ)) ) 
                {
//                         //make edges!!!
//                           System.out.println(edgeID + "make edges!!!");
                    makeEdge(edgeID++, edges1.get(i).l, point_cloud1.get(edgeJ));
                    makeEdge(edgeID++, edges1.get(i).r, point_cloud1.get(edgeJ));
//                    face.add(new Face(edges1.get(i).l, point_cloud1.get(edgeJ), edges1.get(i).r));
                    ui.jProgressBar1.setValue(100 * loading++ / amount);
                    face.add(new Face(point_cloud1.indexOf(edges1.get(i).l), edgeJ, point_cloud1.indexOf(edges1.get(i).r)));
                    point_cloud1.get(edgeJ).setUsed();
//                            break; // skoncim hladanie bodu for j
                }
            }
        }
    }
//------------------------------------------------------------------------------
    
    /**
     * samotna triangulacia GPGPU
     */
    private void CLtriangulate() {
        
        CLtriangulation tri = new CLtriangulation( firstTriangle, point_cloud1, amount );
        
        if (tri.getEdges() == null) {
            System.out.println("CL ERROR, CLtriangulation, returns NULL edges");
        }
        
//        edges1.addAll(tri.getEdges());
        edges1=(tri.getEdges());
        face.addAll(tri.getFace());
        timeGpu = tri.getTime();
        deviceName = tri.getDev();
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

        return (double) Math.sqrt((double) (dx * dx + dy * dy + dz * dz));
    }

    /**
     * distance from EDGE
     */
    public double distanceFromEdge(Edge e, Point b) {
        double dx, dy, dz;
        double x, y, z;

        x = (e.l.getX() + e.r.getX()) / 2;
        y = (e.l.getY() + e.r.getY()) / 2;
        z = (e.l.getZ() + e.r.getZ()) / 2;

        dx = x - b.getX();
        dy = y - b.getY();
        dz = z - b.getZ();
        //System.out.println((double)Math.sqrt((double)(dx * dx + dy * dy)));
        return (double) Math.sqrt((double) (dx * dx + dy * dy + dz * dz));
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

        double x, y, z;
        x = (left.getX() + right.getX()) / 2;
        y = (left.getY() + right.getY()) / 2;
        z = (left.getZ() + right.getZ()) / 2;
//todo: odstranit pretypovanie na int
        Point s = new Point((int) x, (int) y, (int) z);
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
                if (cc.getR() == 0.0) {
                    return -2; //ked su body kolinearne
                }
//                if (cc.isInside(point_cloud1.get(i))) {
                if (cc.isInside1(point_cloud1.get(i)) == 1 ) {  // <<<<<<<<<<<<<
//                    return i;  //bod i je dnuka
                    return -3;  
                }
                else if(cc.isInside1(point_cloud1.get(i)) == 0){ 
                    circlesA.add(cc); 
                    return i; //bod je na kruznici
                }
            }
        } 
        circlesA.add(cc); //adding value to ArrayList
//      cc.setvalid();
        return - 1; //ked nie je vo vnutri 3uholnika dalsi bod
    }    

    
    /**
     * delaunay circumcircle
     */
    private int circleHasPoint1(Point a, Point b, Point c) {
        Circle cc = circumcircle(a, b, c);
               
        for (int i = 0; i < point_cloud1.size(); i++) {
//            if (i == a.getID() || i == b.getID() || i == c.getID() ) {} //do nothing
            if (point_cloud1.get(i).getID() == a.getID() || point_cloud1.get(i).getID() == b.getID() || point_cloud1.get(i).getID() == c.getID() ) {}
            else {
                if (cc.getR() == 0.0) {
                    return -2; //ked su body kolinearne
                }
                if (cc.isInside(point_cloud1.get(i))) {
//                if (cc.isInside1(point_cloud1.get(i)) == 1 ) {  // <<<<<<<<<<<<<
                    return i;  //bod i je dnuka
                }
            }
        } 
        circlesA.add(cc);
        return - 1; //ked nie je vo vnutri 3uholnika dalsi bod
    }    
    
    /**
     * Compute the circle defined by three points (circumcircle).
     */
    private Circle circumcircle(Point p1,Point p2,Point p3) {
        double polomer = -1; 
        double [] AC = new double[3];
        double [] BC = new double[3];
        Point acxbc;
        double spodok, ac2, bc2 ;
        Double circleX, circleY, circleZ;
        circleX = circleY = circleZ = new Double(0);

        polomer = crossProduct1(p1, p2, p3); //check colinear points
        if (polomer != 0.0){
            
            AC[0] = p1.getX()-p3.getX() ;
            AC[1] = p1.getY()-p3.getY() ;
            AC[2] = p1.getZ()-p3.getZ() ;
            BC[0] = p2.getX()-p3.getX() ;
            BC[1] = p2.getY()-p3.getY() ;
            BC[2] = p2.getZ()-p3.getZ() ;
            
            ac2 = AC[0]*AC[0] + AC[1]*AC[1] + AC[2]*AC[2];
            bc2 = BC[0]*BC[0] + BC[1]*BC[1] + BC[2]*BC[2];

            acxbc = vektorovySucin(new Point(AC[0], AC[1], AC[2]), new Point(BC[0], BC[1], BC[2]));
            spodok = (acxbc.getX()*acxbc.getX() + acxbc.getY()*acxbc.getY() + acxbc.getZ()*acxbc.getZ())*2;
            acxbc = vektorovySucin(new Point(ac2*BC[0] - bc2*AC[0], ac2*BC[1] - bc2*AC[1], ac2*BC[2] - bc2*AC[2]), acxbc );
            
            circleX = acxbc.getX()/spodok + p3.getX();
            circleY = acxbc.getY()/spodok + p3.getY();
            circleZ = acxbc.getZ()/spodok + p3.getZ();
            
            polomer = distance(new Point( circleX, circleY, circleZ), p1); //Polomer
        }
        return new Circle(polomer,circleX,circleY,circleZ);
    }

    /**
     * vektorovy sucin pre 2D
     * @param p1
     * @param p2
     * @param p3
     * @return 
     */
    static double crossProduct(Point p1, Point p2, Point p3) {
        double u1, v1, u2, v2;

        u1 =  p2.getX() - p1.getX();
        v1 =  p2.getY() - p1.getY();
        u2 =  p3.getX() - p1.getX();
        v2 =  p3.getY() - p1.getY();

        return u1 * v2 - u2 * v1;
    }
    
    /**
     * vektorovy sucin pre 3D
     * @param p1
     * @param p2
     * @param p3
     * @return 
     */
    static double crossProduct1(Point p1, Point p2, Point p3) {
        double a1, a2, a3, b1, b2, b3;
        
        a1 = p2.getX() - p1.getX() ;
        a2 = p2.getY() - p1.getY() ;
        a3 = p2.getZ() - p1.getZ() ;
        b1 = p3.getX() - p1.getX() ;
        b2 = p3.getY() - p1.getY() ;
        b3 = p3.getZ() - p1.getZ() ;
        
        double ret = a2*b3-a3*b2 + a3*b1-a1*b3 + a1*b2-a2*b1;
        return ret;
    }
    
    static Point vektorovySucin(Point a, Point b) {
        double x, y, z;

        x =  a.getY()*b.getZ() - a.getZ()*b.getY();
        y =  a.getZ()*b.getX() - a.getX()*b.getZ();
        z =  a.getX()*b.getY() - a.getY()*b.getX();

        return new Point(x, y, z);
    }
    
    /**
     * http://www.java-gaming.org/topics/how-can-i-find-the-angle-between-two-lines/3332/msg/30950/view.html#msg30950
     * dot is the cos of angle and the angle is:
     *   angle = Math.acos(dot)
     * @param p1
     * @param p2
     * @param p3
     * @return 
     */
    static double skalarSucin(Point p1, Point p2, Point p3){
        double ax  = p2.getX() - p1.getX();
        double ay  = p2.getY() - p1.getY();
        double az  = p2.getZ() - p1.getZ();
        double bx  = p3.getX() - p1.getX();
        double by  = p3.getY() - p1.getY();
        double bz  = p3.getZ() - p1.getZ();
        double dota= Math.sqrt(ax*ax+ay*ay+az*az);
        double dotb= Math.sqrt(bx*bx+by*by+bz*bz);
        double ret = 3*dota*dotb;
        
        return  ret;
    }

    /**
 * vyriesi problem optimalneho startu
 * daj mi optimalny bod! Iba vypocita MIN vzdialenost a AVG vzdialenost medzi bodmi a vrati najvyhodnejsi bod, co je stale 1.BOD
 * musime zavolat metriku.getPoint inak nevytvori hodnoty AVG a MIN pre body
 * @param amount
 * @param point_cloud
 * @return
 */
    private int getOptimalStartPoint(boolean parameter, ArrayList pc, int amount) {
        long start = System.currentTimeMillis();
        metrika m = new metrika(pc, amount);
        int i = m.getPoint();
        long end = System.currentTimeMillis();
        timeCpuMetrika = end - start;

//zorad pole podla ... toho co je v      public int compareTo(Object obj) {
        if (parameter) { //parameter = sort ano/nie
System.out.println("befor sort"+point_cloud1.toString());
            point_cloud1 = m.sort();
System.out.println("after sort"+point_cloud1.toString());
            return 0;  //najvyhodnejsi je navrchu listu, preto netreba nam uz pouzit ret i;
        }

System.out.println("NO sort"+point_cloud1.toString());
        return i;
    }
    
    /**
 * vyriesi problem optimalneho startu prostrednictvom GPU
 * daj mi optimalny bod! Iba vypocita MIN vzdialenost a AVG vzdialenost medzi bodmi a vrati najvyhodnejsi bod, co je stale 1.BOD
 * musime zavolat metriku.getPoint inak nevytvori hodnoty AVG a MIN pre body
 * @param amount
 * @param point_cloud
 * @return
 */
    private int CLgetOptimalStartPoint(boolean parameter, ArrayList pc, int amount) {
        CLmetrika m = new CLmetrika(pc, amount);
        int i = m.getPoint();
        timeGpuMetrika = m.getTime();

//zorad pole podla ... toho co je v      public int compareTo(Object obj) {
        if (parameter) { //parameter = sort ano/nie
System.out.println("befor sort"+point_cloud1.toString());
            point_cloud1 = m.sort();
System.out.println("after sort"+point_cloud1.toString());
            return 0;  //najvyhodnejsi je navrchu listu, preto netreba nam uz pouzit ret i;
        }

System.out.println("NO sort"+point_cloud1.toString());
        return i;
    }

    /**
 * zaokruhlenie na pozadovany pocet des miest
 * @param val  - jake cislo
 * @param i   - na kolko des. miest
 * @return
 */
    private Double round(Double val, int i) {
        double p =  Math.pow(10, i);
        val = val * p;
        double tmp = Math.round(val);
        return  tmp / p;
    }
    
    /**
     * potvrdzuje existenciu hrany medzi zadanymi bodmi
     * @param x
     * @param y
     * @return 
     */
    private boolean edgeExist(Point x, Point y) {
        for (int i = 0; i < edges1.size(); i++) {
            if ( 
                    ((edges1.get(i).l.getX() == x.getX()) &&  (edges1.get(i).l.getY() == x.getY()) && (edges1.get(i).l.getZ() == x.getZ()) &&
                    (edges1.get(i).r.getX() == y.getX())  &&  (edges1.get(i).r.getY() == y.getY()) && (edges1.get(i).r.getZ() == y.getZ()) ) ||
                    ((edges1.get(i).r.getX() == x.getX()) &&  (edges1.get(i).r.getY() == x.getY()) && (edges1.get(i).r.getZ() == x.getZ()) &&
                    (edges1.get(i).l.getX() == y.getX())  &&  (edges1.get(i).l.getY() == y.getY()) && (edges1.get(i).l.getZ() == y.getZ()))
                    ) {
                return true;
            }
        }
        return false;
    }

    /**
     * spojenie velmi blizkych bodov v ramci zadanej tolerancie OPENCL
     * @param tol 
     */
    private ArrayList CLcollapse(double tol) {
//        throw new UnsupportedOperationException("do not use");
//        
        System.out.println("CL collapse: tolerance "+tol);
        CLcollapse col = new CLcollapse((float)tol, point_cloud1, amount);
        ArrayList<Point> points = col.getReturnArray();
        if (points == null) {
            System.out.println("CL ERROR, point clound returns NULL");
        }
        timeGpuCollapse = col.getTime();
        return  points;
    }
    
    /**
     * spojenie velmi blizkych bodov v ramci zadanej tolerancie
     * @param tol 
     */
    private ArrayList collapse(Double tol) {
        Double dis;
        ArrayList<Point> ppc = new ArrayList<Point>(); 
        Point[] ppp = new Point[point_cloud1.size()];

long start = System.currentTimeMillis();
System.out.println("a>>>>" + point_cloud1.size() + "<<<" + point_cloud1.toString());
        
        for (int i = 0; i < point_cloud1.size(); i++) {
            ppp[i] = point_cloud1.get(i);
        }
        
        for (int i = 0; i < ppp.length; i++) {
            for (int j = 0; j < ppp.length; j++) {
                ui.jProgressBar2.setValue(100*i/ppp.length);
                if (i != j && ppp[i] != null && ppp[j] != null) {
                    dis = distance(ppp[i], ppp[j]);
                    if (0 >= dis.compareTo(tol)) {
                        ppp[i] = new Point(i, round((ppp[i].getX() + ppp[j].getX()) / 2,10), round((ppp[i].getY() + ppp[j].getY()) / 2,4), round((ppp[i].getZ() + ppp[j].getZ()) / 2,10)); 
                        //toto vrati aj viac bodov s rovnakym pointID
                        ppp[j] = null;
                    }
                }
            }
        }
        
        
        int j = 0; //prepisanie ciselID bodov, kedze mohli vzniknut viacere s rovn. cislomID
        for (int i = 0; i < ppp.length; i++) {
            if (ppp[i] != null) {
                ppp[i].setID(j++);
                ppc.add(ppp[i]);
            }
        }
System.out.println("b>>>>" + ppc.size() + "X"+j + "<<<" + ppc.toString());
long end = System.currentTimeMillis();
timeCpuCollapse = end - start;
        return ppc;
    }

    /**
     * export to OBJ file format
     */
    private void exportOBJ()  {
//        Point p;
        ui.setStatus2("Ukladanie bodov do OBJ súboru");
        
        FileWriter ffile = null;
        try {
            ffile = new FileWriter("mesh.obj");
            BufferedWriter out = new BufferedWriter(ffile);
            out.write("\n # Simple Wavefront OBJ file ");
            out.write("\n # Triangulation algorithm of Boris Nikolaevich Delaunay, code by Dominik Januvka. 2011-2012 \n");
            //export
            
            //Vertex data: v Geometric vertices  
            /*
            vt Texture vertices
            vn Vertex normals
            vp Parameter space vertices
            */
            out.write("\n ");
            for (int i = 0; i < point_cloud1.size(); i++) {
                out.write("\n v " + point_cloud1.get(i).getX() + " "+ point_cloud1.get(i).getY() + " "+ point_cloud1.get(i).getZ() );
            }
            out.write("\n ");
            
            //Elements: f Face
            for (int i = 0; i < face.size(); i++) {
//                point_cloud1.indexOf(face.get(i)).;
                out.write("\n f " + face.get(i).toString() ); //todo: vrati ID pointov / neviem ci to je dobre?
            }
            
            
            //koniec
            out.close();
            System.out.println("EXPORT: Váš súbor bol úspešne zapísaný");
            ui.setStatus2("EXPORT: Váš súbor bol úspešne zapísaný");
        } catch (IOException ex) {
            Logger.getLogger(Triangulate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                ffile.close();
            } catch (IOException ex) {
                Logger.getLogger(Triangulate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * export to WRL file format
     * http://www.wiley.com/legacy/compbooks/vrml2sbk/ch13/13fig13.htm
     */
    private void exportWRL()  {
//        Point p;
        ui.setStatus2("Ukladanie bodov do WRL súboru");
        
        FileWriter ffile = null;
        try {
            ffile = new FileWriter("mesh.wrl");
            BufferedWriter out = new BufferedWriter(ffile);
            out.write("\n # VRML V2.0 utf8 ");
            out.write("\n # Triangulation algorithm of Boris Nikolaevich Delaunay, code by Dominik Januvka. 2011-2012 \n");
            //export
            
            //Vertex data
            out.write("\n Shape {\n    }\n    geometry IndexedFaceSet {\n        coord Coordinate {\n            point [");
            for (int i = 0; i < point_cloud1.size(); i++) {
                out.write("\n                " + point_cloud1.get(i).getX() + " "+ point_cloud1.get(i).getY() + " "+ point_cloud1.get(i).getZ() + "," );
            }
            out.write("\n            ]\n        }\n        coordIndex [");
            
            //Faces
            for (int i = 0; i < face.size(); i++) {
                out.write("\n            " + face.get(i).x +", "+face.get(i).y+", "+face.get(i).z+", -1," ); //todo: vrati ID pointov / neviem ci to je dobre?
            }
            out.write("\n        ]\n    }\n}");
            
            //koniec
            out.close();
            System.out.println("EXPORT: Váš súbor bol úspešne zapísaný");
            ui.setStatus2("EXPORT: Váš súbor bol úspešne zapísaný");
        } catch (IOException ex) {
            Logger.getLogger(Triangulate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                ffile.close();
            } catch (IOException ex) {
                Logger.getLogger(Triangulate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * export to ASE file format
     * http://wiki.beyondunreal.com/Legacy:ASE_File_Format
     */
    private void exportASE()  {
        ui.setStatus2("Ukladanie bodov do ASE súboru");
        
        FileWriter ffile = null;
        try {
            ffile = new FileWriter("mesh.ase");
            BufferedWriter out = new BufferedWriter(ffile);
            out.write("\n *COMMENT \"AsciiExport Version \" ");
            out.write("\n *COMMENT Triangulation algorithm of Boris Nikolaevich Delaunay, code by Dominik Januvka. 2011-2012 \n");
            out.write("\n *GEOMOBJECT { \n 	*NODE_NAME \"mesh\" \n	*MESH { \n");
            //export

            out.write("\n		*MESH_NUMVERTEX "+ point_cloud1.size() +" \n		*MESH_VERTEX_LIST {");
            for (int i = 0; i < point_cloud1.size(); i++) {
                out.write("\n *MESH_VERTEX "+ i +" " + point_cloud1.get(i).getX() + " "+ point_cloud1.get(i).getY() + " "+ point_cloud1.get(i).getZ() );
            }
            out.write("\n 		}\n		*MESH_NUMFACES "+face.size()+" \n		*MESH_FACE_LIST {");
            
            //Face
            for (int i = 0; i < face.size(); i++) {
                out.write("\n *MESH_FACE    "+i+":    A:    "+face.get(i).x+" B:    "+face.get(i).y+" C:    "+face.get(i).z+" AB:    1 BC:    1 CA:    0	 *MESH_SMOOTHING 1 	*MESH_MTLID 0 "   ); 
            }
            out.write("\n			}\n	}\n}");
            
            
            //koniec
            out.close();
            System.out.println("EXPORT: Váš súbor bol úspešne zapísaný");
            ui.setStatus2("EXPORT: Váš súbor bol úspešne zapísaný");
        } catch (IOException ex) {
            Logger.getLogger(Triangulate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                ffile.close();
            } catch (IOException ex) {
                Logger.getLogger(Triangulate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * zapis statistiky
     */
    private void stat()  {
        
        FileWriter ffile = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date date = new Date();
//            System.out.println(dateFormat.format(date));
            
            ffile = new FileWriter("stat"+dateFormat.format(date)+".txt");
            BufferedWriter out = new BufferedWriter(ffile);
            if (deviceName==null) {
                out.write("\ndevice: CPU");
            } else {
                out.write("\ndevice: "+deviceName);
            }
            out.write("\n\ntriangulácia: (ms)");
            out.write("\n GPU čas : "+timeGpu);
            out.write("\n CPU čas : "+timeCpu);
            out.write("\n --------------------------");
            out.write("\nmetrika: (ms)");
            out.write("\n GPU čas : "+timeGpuMetrika);
            out.write("\n CPU čas : "+timeCpuMetrika);
            out.write("\n --------------------------");
            out.write("\nspojenie bodov: (ms)");
            out.write("\n GPU čas : "+timeGpuCollapse);
            out.write("\n CPU čas : "+timeCpuCollapse);
            out.write("\n --------------------------");
            out.write("\n model       : "+file);
            out.write("\n počet bodov : "+amount);
            out.write("\n\n náhodný štart:"+startRandom);
            out.write("\n triedenie   : "+sort);
            out.write("\n tolerancia  : "+tolerancia);
            out.write("\n konštanta 1 : "+kon1);
            out.write("\n konštanta 2 : "+kon2);
            
            
            //koniec
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Triangulate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                ffile.close();
            } catch (IOException ex) {
                Logger.getLogger(Triangulate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
