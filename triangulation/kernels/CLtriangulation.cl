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
                            dist = Math.acos(skalarSucin(edges1.get(edgeID-1).getMidpoint(), edges1.get(i).getMidpoint(), point_cloud1.get(j)));
//                            dist = circlesA.get(circlesA.size()-1).getR();  //posledny pridany kruh neobsahujuci bod
//                            dist = distanceFromEdge(edges1.get(i), point_cloud1.get(j));  
                            if (point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).l, point_cloud1.get(j)) 
                                    || point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).r, point_cloud1.get(j)) ) 
                            {
//                                if (dist.compareTo(dist_last) < 0) {
                                if (dist.compareTo(dist_last) > 0) {
                                    dist_last = dist;
                                    edgeJ = j;
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
//                            break;
                            dist = Math.acos(skalarSucin(edges1.get(edgeID-1).getMidpoint(), edges1.get(i).getMidpoint(), point_cloud1.get(j)));
//                            dist = circlesA.get(circlesA.size()-1).getR();
//                            dist = distanceFromEdge(edges1.get(i), point_cloud1.get(j));  
                            if (point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).l, point_cloud1.get(j)) 
                                    || point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).r, point_cloud1.get(j)) ) 
                            {
//                                if (dist.compareTo(dist_last) < 0) {
                                if (dist.compareTo(dist_last) > 0) {
                                    dist_last = dist;
                                    edgeJ = j;
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




__kernel void CLtriangulation
            (__global const float *x,
             __global const float *y,
             __global const float *z,
             int valid,
             int edge,
             __global int *face,
             int n,
             __global int *firstTriangle,
            float dist_last)
{
    float dx;
    float dy;
    float dz;
    float sum;
    float tmp;

    int gid = get_global_id(0);
    
    for (int i = 0; i < n; i++) {
        dx = x[i] - x[gid];
        dy = y[i] - y[gid];
        dz = z[i] - z[gid];
        tmp = sqrt(dx*dx + dy*dy + dz*dz);
        
//        if(dist_last >= tmp && i != gid  ){
        if(dist_last >= tmp && tmp != 0.0){
            dist_last = tmp;
        }
        sum = sum + tmp;
    }
    avg[gid] = sum/n;
    min[gid] = dist_last;
    
    
    
}