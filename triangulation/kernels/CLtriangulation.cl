//dominik januvka 2012
//#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable
#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable
#pragma OPENCL EXTENSION cl_khr_local_int32_base_atomics : enable
//#pragma OPENCL EXTENSION cl_khr_global_int32_extended_atomics : enable
//#pragma OPENCL EXTENSION cl_khr_local_int32_extended_atomics : enable


inline float dist(float3 a, float3 b)
{
    float dx = a.x - b.x;
    float dy = a.y - b.y;
    float dz = b.z - b.z;
    float tmp = sqrt(dx*dx + dy*dy + dz*dz);
    return tmp;
}

inline float3 dot1(float3 a, float3 b) {
        float x, y, z;

        x =  a.y*b.z - a.z*b.y;
        y =  a.z*b.x - a.x*b.z;
        z =  a.x*b.y - a.y*b.x;

        return (float3)(x, y, z);
}


inline float4 circleHasPoint1(float3 a, float3 b, float3 c) {
    
    float3 ac  = a-c;
    float3 acd = dot(ac,ac);
    float3 bc = b-c;
    float3 bcd = dot(bc,bc);
    float3 aaa = acd * bc - bcd * ac;
    
    float3 bbb = cross(ac, bc);
    float3 ccc = cross(aaa,bbb);
    float3 ddd = 2 * dot(bbb, bbb);
    float3 eee = ccc/ddd;
    
    eee = eee + c;
        
    float polomer = distance( a, eee); //Polomer  !!!!!!!>>>> distance skontrolovat!!!!<<<<
    
//    if( isnan(eee.x)==0 || isnan(eee.y)==0 || isnan(eee.z)==0 ) polomer = 0.0;
     if( isnan(polomer) ) polomer = 0;
    
//    if( dot(cross(bc, ac),cross(bc, ac)) == 0 ) polomer = -1;
//    polomer = dot(cross(bc, ac),cross(bc, ac)) ;
    
    return (float4) ( eee, polomer ) ;
    }
    
    
    
    
inline float4 circleHasPoint(float ax,float ay,float az, 
                          float bx,float by,float bz, 
                          float cx,float cy,float cz) {
        float cc_x = -1;
        float cc_y = -1;
        float cc_z = -1;
        float polomer = -1; 
        
//        Point acxbc;
        float spodok, ac2, bc2 ;
        float ac00, ac11, ac22, bc00, bc11, bc22 ;
        
        
        float a1, a2, a3, b1, b2, b3;
        a1 = bx - ax ;
        a2 = by - ay ;
        a3 = bz - az ;
        b1 = cx - ax ;
        b2 = cy - ay ;
        b3 = cz - ax ;
        polomer  = a2*b3-a3*b2 + a3*b1-a1*b3 + a1*b2-a2*b1; //check colinear points

        if (polomer != 0.0){
            
            ac00 = ax - cx;
            ac11 = ay - cy;
            ac22 = az - cz;
            bc00 = bx - cx;
            bc11 = by - cy;
            bc22 = bz - cz;
            
            ac2 = ac00*ac00 + ac11*ac11 + ac22*ac22;
            bc2 = bc00*bc00 + bc11*bc11 + bc22*bc22;


            float3 yyy = dot1((float3)(ac00, ac11, ac22),(float3)(bc00, bc11, bc22));
            spodok = (yyy.x*yyy.x + yyy.y*yyy.y + yyy.z*yyy.z)*2;
            yyy = dot1((float3)(ac2*bc00 - bc2*ac00, ac2*bc11 - bc2*ac11, ac2*bc22 - bc2*ac22), yyy );
            

            cc_x = (yyy.x/spodok) + cx;
            cc_y = (yyy.y/spodok) + cy;
            cc_z = (yyy.z/spodok) + cz;
            
            polomer = dist( (float3)( cc_x,cc_y,cc_z), (float3)(ax,ay,az)); //Polomer 
/* <i> */           if( !isnan(polomer) ) polomer = 0.0;
        }
        return (float4)(cc_x,cc_y,cc_z,polomer);
}




inline int isInside1(float4 xxx, float3 ppp){ 
        float a = distance((float3)( xxx.x,xxx.y,xxx.z),ppp);
        float b = xxx.w;
        if ( a < b ){
            if ( a == b ) {
                return 0; //ked su body na jednej kruznici
            }
            return 1; //true, bod lezi v kruznici 
        }
        else
            return -1; //false //ked nie je vo vnutri 3uholnika dalsi bod
    }
    
    
    
    
 bool edgeExist (int edgeA, 
                int edgeB, 
                __global int *edgeL, 
                __global int *edgeR){
    for (int i = 0; i < sizeof(edgeL); i++) {
        if (
//            ((edgeR[i] == edgeR[edgeA]) &&  (edgeL[i] == edgeL[edgeB])) ||
//            ((edgeL[i] == edgeL[edgeA]) &&  (edgeR[i] == edgeR[edgeB]))
            ((edgeR[i] == edgeA) &&  (edgeL[i] == edgeB)) ||
            ((edgeL[i] == edgeA) &&  (edgeR[i] == edgeB))
            ) {
            return true;
            }
        }
    return false;
    }
        

        

//kernel
__kernel void CLtriangulation
            (__global const float *x,//0
             __global const float *y,//1
             __global const float *z,//2
             __global  float *min,//3
             __global int *validP, //4
             __global int *edgeL,  //5
             __global int *edgeR,  //6
             __global int *validE, //7
             __global int *faceV1,
             __global int *faceV2,
             __global int *faceV3,
             int n,                 //11
             float dist_max,        //12
             __global int *id,
             __global int *idEdge_lock,
             __global int *idFace_lock )
{
    float dis;
    float dist_last = dist_max;
    float tmp;
//    int   xxx;
    float4   xxx;
    int isInside, edgeID, faceID;
    int edgeJ = -1;

    int gid = get_global_id(0);
    
//    while(validE[0] == -1){
//        wait
//    }

    int exx = 1;
    int counter = 1;
    int wait = 1;

    while (wait) {
        //mozem kontrolovat ci som pouzil vsetky body z point cloudu a pripadne skoncit -> to je neaky typ timeout-u
        //moze nastat ze strianguluje a zostane neaky pocet miest pre hrany nevyuzity, tj ze sa nespustia niektore poseldna jadra a tak sa program zacykli
        //naopak ak sa vycerpaju zdroje (pole urcene pre nove hrany) model nemusi byt cely
        exx = 1;
        for (int k = 0; k < n; k++) {
            if(validP[k]==1) exx = 0;
        }
        if(exx == 1) {
            wait = 0;
//edgeR[gid]=-4;
            return;  //tvrdy koniec
        }
        
            
if(validE[gid]==1) {wait = 0;}    
if(gid==0 || gid==1 || gid==2) {wait = 0;}    
        
        
        if(counter==10000*gid || counter>100000) { //variabilna cakacia doba
            wait = 0; 
//if(counter==8000*gid )edgeR[gid]=-6;
//if( counter>160000)edgeR[gid]=-7;
            return;
            }
        counter++;
        
    }






//        for (gid = 0; gid < 9*n; gid++) {
//         edgeJ = -1;
//         dist_last = dist_max;

    //4/for used points only
            for (int j = 0; j < n; j++) {
                if (validP[j] < 0) {
                    if (edgeL[gid] == j || edgeR[gid] == j) {
                    } else {
                    isInside = -1;
                          xxx = circleHasPoint1((float3) (x[edgeL[gid]],y[edgeL[gid]],z[edgeL[gid]]), 
                              (float3) (x[edgeR[gid]],y[edgeR[gid]],z[edgeR[gid]]),
                              (float3) (x[j], y[j], z[j]) );
//                        xxx = circleHasPoint(x[edgeL[gid]],y[edgeL[gid]],z[edgeL[gid]], 
//                                             x[j], y[j], z[j],
//                                             x[edgeR[gid]], y[edgeR[gid]],z[edgeR[gid]]);
                        for (int i = 0; i < n; i++) {
                        if (edgeL[gid] == i || j == i || edgeR[gid] == i ) {} //do nothing
                        else {
                            isInside = isInside1(xxx,(float3)(x[i], y[i], z[i]));
                            }
                        }
                        
                        if ( xxx.w != 0 && isInside <= 0) {



//                            dist = Math.acos(skalarSucin(edges1.get(edgeID-1).getMidpoint(), edges1.get(i).getMidpoint(), point_cloud1.get(j)));
//                            dist = circlesA.get(circlesA.size()-1).getR();  //posledny pridany kruh neobsahujuci bod
//                            dist = distanceFromEdge(edges1.get(i), point_cloud1.get(j));  
                            dis = distance((float3) ( ( x[edgeL[gid]]+x[edgeR[gid]] ) /2,
                                                      ( y[edgeL[gid]]+y[edgeR[gid]] ) /2,
                                                      ( z[edgeL[gid]]+z[edgeR[gid]] ) /2) , 
                                                      (float3)(x[j], y[j], z[j]) );  
                                                    
//                            if (point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).l, point_cloud1.get(j)) 
//                                    || point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).r, point_cloud1.get(j)) ) 
                            {
                                if (dis < dist_last) {
                                    dist_last = dis;
                                    edgeJ = j;
                                }
                            }
                        }
                    }
                }
            }
            if (edgeJ != -1) {
    if (min[edgeJ]*5 > distance( (float3) (x[edgeL[gid]],y[edgeL[gid]],z[edgeL[gid]]) , (float3)(x[edgeJ], y[edgeJ], z[edgeJ])) 
     || min[edgeJ]*5 > distance( (float3) (x[edgeR[gid]],y[edgeR[gid]],z[edgeR[gid]]) , (float3)(x[edgeJ], y[edgeJ], z[edgeJ]))   ) 
                        {
                            //buď dve hrany a skončim, alebo jedna a skončim.
//                            if (!edgeExist(edgeL[gid], edgeJ, edgeL, edgeR) 
//                             && !edgeExist(edgeR[gid], edgeJ, edgeL, edgeR)) {
                                
                                
                                
                                
bool aaa = false;
bool bbb = false;
   for (int h = 0; h < 3*3*n; h++) {
        if (((edgeR[h] == edgeL[gid]) &&  (edgeL[h] == edgeJ)) ||
            ((edgeL[h] == edgeL[gid]) &&  (edgeR[h] == edgeJ))
            ) {
            bbb = true;
//            break;
            }
        if (((edgeR[h] == edgeR[gid]) &&  (edgeL[h] == edgeJ)) ||
            ((edgeL[h] == edgeR[gid]) &&  (edgeR[h] == edgeJ))
            ) {
            aaa = true;
//            break;
            }
        }
                                
                                
                                if( !aaa && !bbb ){
                                
                                
                                
                                validP[edgeJ] = -1;
        
                                int waiting = 1;                               
                                while (waiting) {
                                    if (!atomic_xchg(idEdge_lock, 1)) {
                                        // critical section
                                        edgeID = id[0];
                                        id[0] = id[0] + 2;
                                        if(validE[edgeID]==1){
                                            edgeID++;
                                            id[0]++;
                                        }
                                        atomic_xchg(idEdge_lock, 0);
                                        waiting = 0;
                                    }
                                }
//                                if(edgeID < sizeof(edgeL)){
                                if(edgeID < 3*3*n){
                                    edgeL[ edgeID ] = edgeL[gid];
                                    edgeR[ edgeID ] = edgeJ;
                                    validE[ edgeID ] = 1;

                                    edgeID = edgeID + 1;

                                    edgeL[ edgeID ] = edgeR[gid];
                                    edgeR[ edgeID ] = edgeJ;
                                    validE[ edgeID ] = 1;
                                    } else {   // ak sa vycerpaju zdroje (pole urcene rpe nove hrany) model nemusi byt cely
                                    if(edgeID-1 < 3*3*n){
                                        edgeID = edgeID - 1;
                                        edgeL[ edgeID ] = edgeL[gid];
                                        edgeR[ edgeID ] = edgeJ;
                                        validE[ edgeID ] = 1;
                                  }
                               }
////---------
//                            if(gid*2+4 < 2*3*n){
//
//                                edgeL[ gid*2+3 ] = edgeL[gid];
//                                edgeR[ gid*2+3 ] = edgeJ;
//                                validE[ gid*2+3 ] = 1;
//
//                                edgeL[ gid*2+4 ] = edgeR[gid];
//                                edgeR[ gid*2+4 ] = edgeJ;
//                                validE[ gid*2+4 ] = 1;
//                                } else{
//                                    if(gid*2+3 < 2*3*n){
//                                        edgeL[ gid*2+3 ] = edgeL[gid];
//                                        edgeR[ gid*2+3 ] = edgeJ;
//                                        validE[ gid*2+3 ] = 1;
//                                  }
//                               }
////---------
                                

                                //fACE
                                waiting = 1;
                                while (waiting) {
//                                    if (!atomic_xchg(id[2], 1)) 
                                    if (!atomic_xchg(idFace_lock, 1)) {
                                        // critical section
                                        faceID = id[1];
                                        id[1] = id[1] + 1;
                                        atomic_xchg(idFace_lock, 0);
                                        waiting = 0;
                                    }
                                }
                                faceV1[ faceID ] = edgeL[gid];
                                faceV2[ faceID ] = edgeJ;
                                faceV3[ faceID ] = edgeR[gid];
                                
                            } else {
//                                if (!edgeExist(edgeL[gid], edgeJ, edgeL, edgeR) ) {
                                if( !aaa ){
                                    validP[edgeJ] = -1;
                                        //make edge
                                        int waiting = 1;                               
                                        while (waiting) {
                                            if (!atomic_xchg(idEdge_lock, 1)) {
                                                // critical section
                                                edgeID = id[0];
                                                id[0] = id[0] + 1;
                                                if(validE[edgeID]==1){
                                                    edgeID++;
                                                    id[0]++;
                                                }
                                                atomic_xchg(idEdge_lock, 0);
                                                waiting = 0;
                                            }
                                        }
                                        if(edgeID < 3*3*n){
                                            edgeL[ edgeID ] = edgeL[gid];
                                            edgeR[ edgeID ] = edgeJ;
                                            validE[ edgeID ] = 1;
                                        }
////---------
//                            if(gid*2+3 < 2*3*n){
//
//                                edgeL[ gid*2+3 ] = edgeL[gid];
//                                edgeR[ gid*2+3 ] = edgeJ;
//                                validE[ gid*2+3 ] = 1;
//
//                                } 
////---------
                                //make face
                                waiting = 1;
                                while (waiting) {
                                    if (!atomic_xchg(idFace_lock, 1)) {
                                        // critical section
                                        faceID = id[1];
                                        id[1] = id[1] + 1;
                                        atomic_xchg(idFace_lock, 0);
                                        waiting = 0;
                                    }
                                }
                                faceV1[ faceID ] = edgeL[gid];
                                faceV2[ faceID ] = edgeJ;
                                faceV3[ faceID ] = edgeR[gid];
                                        
                                } else {
//                                    if (!edgeExist(edgeR[gid], edgeJ, edgeL, edgeR) ) {
                                    if( !bbb ){
                                        validP[edgeJ] = -1;
                                        //make edge
                                        int waiting = 1;
                                        while (waiting) {
                                            if (!atomic_xchg(idEdge_lock, 1)) {
                                                // critical section
                                                edgeID = id[0];
                                                id[0] = id[0] + 1;
                                                if(validE[edgeID]==1){
                                                    edgeID++;
                                                    id[0]++;
                                                }
                                                atomic_xchg(idEdge_lock, 0);
                                                waiting = 0;
                                            }
                                        }
                                        if(edgeID < 3*3*n){
                                            edgeL[ edgeID ] = edgeR[gid];
                                            edgeR[ edgeID ] = edgeJ;
                                            validE[ edgeID ] = 1;
                                            }
////---------
////                            if(gid*2+3 < 2*3*n){
////                                edgeL[ gid*2+3 ] = edgeR[gid];
////                                edgeR[ gid*2+3 ] = edgeJ;
////                                validE[ gid*2+3 ] = 1;
////                                } 
////---------
////                                        //make face
                                    waiting = 1;
                                    while (waiting) {
                                        if (!atomic_xchg(idFace_lock, 1)) {
                                            // critical section
                                            faceID = id[1];
                                            id[1] = id[1] + 1;
                                            atomic_xchg(idFace_lock, 0);
                                            waiting = 0;
                                        }
                                    }
                                    faceV1[ faceID ] = edgeL[gid];
                                    faceV2[ faceID ] = edgeJ;
                                    faceV3[ faceID ] = edgeR[gid];
                                    }
                                }
                                validP[edgeJ] = -1;
//                                    //make face
                                int waiting = 1;
                                while (waiting) {
                                    if (!atomic_xchg(idFace_lock, 1)) {
                                        // critical section
                                        faceID = id[1];
                                        id[1] = id[1] + 1;
                                        atomic_xchg(idFace_lock, 0);
                                        waiting = 0;
                                    }
                                }
                                faceV1[ faceID ] = edgeL[gid];
                                faceV2[ faceID ] = edgeJ;
                                faceV3[ faceID ] = edgeR[gid];
                            }
                        }
            }
    
//----------------------------------

            edgeJ = -1;
            dist_last = dist_max;
            
            //4 not used points only
            for (int j = 0; j < n; j++) {
                if (validP[j] > 0) {

                    if (edgeL[gid] == j || edgeR[gid] == j) {
                    } else {
                        isInside = -1;
                        xxx = circleHasPoint1((float3) (x[edgeL[gid]],y[edgeL[gid]],z[edgeL[gid]]), 
                                              (float3) (x[edgeR[gid]],y[edgeR[gid]],z[edgeR[gid]]),
                                              (float3) (x[j], y[j], z[j]) );
//                        xxx = circleHasPoint(x[edgeL[gid]],y[edgeL[gid]],z[edgeL[gid]], 
//                                             x[edgeR[gid]], y[edgeR[gid]],z[edgeR[gid]],
//                                             x[j], y[j], z[j]);
                        for (int i = 0; i < n; i++) {
                            if (edgeL[gid] == i || j == i || edgeR[gid] == i ) {
                                //nothing
                            } else {
                            if( isInside1(xxx,(float3)(x[i], y[i], z[i])) == 1 ){
                                isInside = 1;
                            }
                          }
                        }
//min[gid] = xxx.w;
//                        if ( xxx.w != 0 && isInside != 1 ) {
                        if ( xxx.w != 0 && isInside <= 0 ) {
                            edgeJ = j;


//                            dist = Math.acos(skalarSucin(edges1.get(edgeID-1).getMidpoint(), edges1.get(i).getMidpoint(), point_cloud1.get(j)));
//                            dist = circlesA.get(circlesA.size()-1).getR();  //posledny pridany kruh neobsahujuci bod
//                            dist = distanceFromEdge(edges1.get(i), point_cloud1.get(j));  
                            dis = distance((float3) ( ( x[edgeL[gid]]+x[edgeR[gid]] ) /2,
                                                      ( y[edgeL[gid]]+y[edgeR[gid]] ) /2,
                                                      ( z[edgeL[gid]]+z[edgeR[gid]] ) /2) , 
                                                      (float3)(x[j], y[j], z[j]) );  
                                                    
//                            if (point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).l, point_cloud1.get(j)) 
//                                    || point_cloud1.get(j).getMin()*kon1 > distance(edges1.get(i).r, point_cloud1.get(j)) ) 
                            {
                                if (dis < dist_last) {
                                    dist_last = dis;
                                    edgeJ = j;
                                }
                            }
                        }
                    }
                }
            }
            

            if (edgeJ != -1) {
if (min[edgeJ]*5 > distance( (float3) (x[edgeL[gid]],y[edgeL[gid]],z[edgeL[gid]]) , (float3)(x[edgeJ], y[edgeJ], z[edgeJ])) 
     || min[edgeJ]*5 > distance( (float3) (x[edgeR[gid]],y[edgeR[gid]],z[edgeR[gid]]) , (float3)(x[edgeJ], y[edgeJ], z[edgeJ]))   )
                        {
                            validP[edgeJ] = -1;
//                            //make edges
                            int waiting = 1;
                            while (waiting) {
                                if (!atomic_xchg(idEdge_lock, 1)) {
                                    // critical section
                                    edgeID = id[0];
                                    id[0] = id[0] + 2;                                    
                                    if(validE[edgeID]==1){
                                        edgeID++;
                                        id[0]++;
                                    }
                                    atomic_xchg(idEdge_lock, 0);
                                    waiting = 0;
                                }
                            }
                            if(edgeID < 3*3*n){
                                edgeL[ edgeID ] = edgeL[gid];
                                edgeR[ edgeID ] = edgeJ;
                                validE[ edgeID ] = 1;

                                edgeID = edgeID + 1;

                                edgeL[ edgeID ] = edgeR[gid];
                                edgeR[ edgeID ] = edgeJ;
                                validE[ edgeID ] = 1;
                                } else{
                                    if(edgeID < 3*3*n){
                                        edgeL[ edgeID ] = edgeL[gid];
                                        edgeR[ edgeID ] = edgeJ;
                                        validE[ edgeID ] = 1;
                                  }
                               }
                            
////---------
//                            if(gid*2+4 < 2*3*n){
//
//                                edgeL[ gid*2+3 ] = edgeL[gid];
//                                edgeR[ gid*2+3 ] = edgeJ;
//                                validE[ gid*2+3 ] = 1;
//
//                                edgeL[ gid*2+4 ] = edgeR[gid];
//                                edgeR[ gid*2+4 ] = edgeJ;
//                                validE[ gid*2+4 ] = 1;
//                                } else{
//                                    if(gid*2+3 < 2*3*n){
//                                        edgeL[ gid*2+3 ] = edgeL[gid];
//                                        edgeR[ gid*2+3 ] = edgeJ;
//                                        validE[ gid*2+3 ] = 1;
//                                  }
//                               }
////---------

                            //make face
                            waiting = 1;
                            while (waiting) {
                                if (!atomic_xchg(idFace_lock, 1)) {
                                    // critical section
                                    faceID = id[1];
                                    id[1] = id[1] + 1;
                                    atomic_xchg(idFace_lock, 0);
                                    waiting = 0;
                                }
                            }
                            faceV1[ faceID ] = edgeL[gid];
                            faceV2[ faceID ] = edgeJ;
                            faceV3[ faceID ] = edgeR[gid];

                        }
                }
}
//}


       
             
            

               
       
