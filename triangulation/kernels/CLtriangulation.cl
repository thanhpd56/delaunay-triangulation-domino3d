__kernel void CLtriangulation
            (__global const float *x,
             __global const float *y,
             __global const float *z,
             __global float *min,
             __global float *avg,
             int n,
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