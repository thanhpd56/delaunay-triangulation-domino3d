////#pragma OPENCL EXTENSION cl_amd_printf:enable
//#ifdef cl_amd_fp64 
//#pragma OPENCL EXTENSION cl_amd_fp64 : enable
//#endif
//#ifdef cl_khr_fp64
//#pragma OPENCL EXTENSION cl_khr_fp64 : enable
//#endif

//#ifdef cl_khr_fp64
//    #pragma OPENCL EXTENSION cl_khr_fp64 : enable
//#elif defined(cl_amd_fp64)
//    #pragma OPENCL EXTENSION cl_amd_fp64 : enable
//#else
//    #error "Double precision floating point not supported by OpenCL implementation."
//#endif

//#ifdef cl_khr_fp64
//#pragma OPENCL EXTENSION cl_khr_fp64 : enable
//#define FLOAT double
//#elif defined(cl_amd_fp64)
//#pragma OPENCL EXTENSION cl_amd_fp64 : enable
//#define FLOAT double
//#else
//#define FLOAT float
//#endif


__kernel void 
   CLmetrika(__global const float *x,
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