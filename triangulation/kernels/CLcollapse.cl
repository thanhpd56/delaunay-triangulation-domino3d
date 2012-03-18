__kernel void 
  CLcollapse(__global const float *x,
             __global const float *y,
             __global const float *z,
             __global  float *cx,
             __global  float *cy,
             __global  float *cz,
             int n,
             float tolerance,
            __global int *valid)
{
    float dx;
    float dy;
    float dz;
    float tmp;
    
    int gid = get_global_id(0);
    valid[gid] = 1;
//    float nan = 0/0; 

    cx[gid] = x[gid];
    cy[gid] = y[gid];
    cz[gid] = z[gid];
    
    for (int i = 0; i < n; i++) {
        if (i != gid) {
            if(valid[gid] == 0) break;
            
            dx = x[i] - x[gid];
            dy = y[i] - y[gid];
            dz = z[i] - z[gid];
            tmp = sqrt(dx*dx + dy*dy + dz*dz);
            
            if(valid[i] == 0) continue;
            if(tmp <= tolerance){
                cx[gid] = (x[i] + x[gid])/2;
                cy[gid] = (y[i] + y[gid])/2;
                cz[gid] = (z[i] + z[gid])/2;
                valid[i] = 0;  //zacykli sa
            }
        }
    }
}





