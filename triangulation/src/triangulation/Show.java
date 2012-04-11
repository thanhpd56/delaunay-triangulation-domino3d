package triangulation;

import java.util.ArrayList;
//import sun.java2d.loops.DrawRect;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Image;
//import javax.swing.JPanel;
//import sun.java2d.loops.DrawLine;
import java.awt.*;
//import java.awt.geom.*;
//import javax.swing.*;



/**
 * Zobrazenie bodov v 2D. Vypnute.
 * @author Domino
 */
public class Show extends javax.swing.JFrame {
//    Point[] point_cloud;
//    Edge[] edges;
    ArrayList<Point> point_cloud1;
    ArrayList<Edge> edges;
    int scale = 10;
//    int scale = 2;
    int offset = 25;
    int offset2 = 200;
    ArrayList circlesA;

    public void paint(Graphics g) {
        //super.paint(g);
//float x;
//float y;
        
        //body XY
        g.setColor(Color.RED);
        try {
            for (int i = 0; i < point_cloud1.size(); i++) {
                g.fillOval((int)point_cloud1.get(i).getX() * scale + offset, (int)point_cloud1.get(i).getY() * scale + offset, 5, 5);
                g.drawString("" + point_cloud1.get(i).getID(), (int)point_cloud1.get(i).getX() * scale + offset, (int)point_cloud1.get(i).getY() * scale + offset);
            }
        } catch (Exception e) {
        }
//        //body XZ
//        g.setColor(Color.RED);
//        try {
//            for (int i = 0; i < point_cloud1.size(); i++) {
//                g.fillOval((int)point_cloud1.get(i).getX() * scale + offset2, (int)point_cloud1.get(i).getZ() * scale + offset2, 5, 5);
//                g.drawString("" + point_cloud1.get(i).getID(), (int)point_cloud1.get(i).getX() * scale + offset2, (int)point_cloud1.get(i).getZ() * scale + offset2);
//            }
//        } catch (Exception e) {
//        }

        //ciary XY
        g.setColor(Color.blue);
        try {
            for (int i = 0; i < edges.size(); i++) {
                g.drawLine((int) edges.get(i).l.getX() * scale + offset, (int) edges.get(i).l.getY() * scale + offset, (int) edges.get(i).r.getX() * scale + offset, (int) edges.get(i).r.getY() * scale + offset);
                if (i == 0 || i == 1 || i == 2) {
                    g.setColor(Color.CYAN);
                    g.drawString("" + point_cloud1.get(i).getID(), (int) point_cloud1.get(i).getX() * scale + offset, (int) point_cloud1.get(i).getY() * scale + offset);
                } else {
                    g.setColor(Color.blue);
                    g.drawLine((int) edges.get(i).l.getX() * scale + offset, (int) edges.get(i).l.getY() * scale + offset, (int) edges.get(i).r.getX() * scale + offset, (int) edges.get(i).r.getY() * scale + offset);
                }
                
//                if(edges.get(i).getMidpoint()!=null)g.setColor(Color.GREEN);g.fillOval((int)edges.get(i).getMidpoint().getX() * scale + offset, (int)edges.get(i).getMidpoint().getY() * scale + offset, 5, 5);
                g.setColor(Color.BLACK);
                g.drawString(""+i,(int)(edges.get(i).l.getX()+edges.get(i).r.getX())/2 * scale + offset, (int)(edges.get(i).l.getY()+edges.get(i).r.getY())/2 * scale + offset);
            } 
        } catch (Exception e) {
        }
//        //ciary XZ
//        g.setColor(Color.blue);
//        try {
//            for (int i = 0; i < edges.size(); i++) {
//                g.drawLine((int)edges.get(i).l.getX() * scale + offset2, (int)edges.get(i).l.getZ() * scale + offset2, (int)edges.get(i).r.getX() * scale + offset2, (int)edges.get(i).r.getZ() * scale + offset2);
//                if (i==0||i==1||i==2) {g.setColor(Color.CYAN);
//                    g.drawString("" + point_cloud1.get(i).getID(), (int)point_cloud1.get(i).getX() * scale + offset2, (int)point_cloud1.get(i).getZ() * scale + offset2);
//                } else {g.setColor(Color.blue);
//                g.drawLine((int)edges.get(i).l.getX() * scale + offset2, (int)edges.get(i).l.getZ() * scale + offset2, (int)edges.get(i).r.getX() * scale + offset2, (int)edges.get(i).r.getZ() * scale + offset2);
//                }
//                
//                if(edges.get(i).getMidpoint()!=null)g.setColor(Color.GREEN);g.fillOval((int)edges.get(i).getMidpoint().getX() * scale + offset2, (int)edges.get(i).getMidpoint().getZ() * scale + offset2, 5, 5);g.setColor(Color.BLACK);g.drawString(""+i,(int)edges.get(i).getMidpoint().getX() * scale + offset2, (int)edges.get(i).getMidpoint().getZ() * scale + offset2);
//                //g.drawString(""+i, edges[i].l.getX()*scale+offset, edges[i].r.getY()*scale+offset );
//            } 
//        } catch (Exception e) {
//        }

//        //kruhy
//        g.setColor(Color.ORANGE);
//        try {
//            for (int i = 0; i < circlesA.size(); i++) {
//                Circle circle = (Circle) circlesA.get(i);
//                g.drawOval(((int)circle.getX()-(int)circle.getR()) * scale + offset,((int)circle.getY()-(int)circle.getR()) * scale + offset,2*(int)circle.getR() * scale,2*(int)circle.getR() * scale);
//            }
//        } catch (Exception e) {
//        }


    }

    /** Creates new form Show */
    public Show() {
        initComponents();
        System.out.print("End of program.");
    }
    
//    void Kresli(triangulation.Point[] point_cloud, Edge[] edges, ArrayList circlesA) {
    void Kresli(ArrayList point_cloud, ArrayList edges, ArrayList circlesA) {
//        paint(new Graphics);
        this.point_cloud1=point_cloud;
        this.edges=edges;
        this.circlesA=circlesA;

    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Show().setVisible(true);
            }
        });
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
