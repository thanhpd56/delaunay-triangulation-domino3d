/*
 * JOCL - Java bindings for OpenCL
 */
package triangulation;

import java.io.*;
import static org.jocl.CL.*;
import org.jocl.*;

import com.sun.org.apache.bcel.internal.generic.FLOAD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A small JOCL.
 * @author Domino
 */
public class CLmetrika
{    
    private int amount;
//    Point[] point_cloud;
    ArrayList<Point> point_cloud1;
//    double[] cloud;


    public CLmetrika(ArrayList point_cloud, int amount) {
        this.point_cloud1=point_cloud;
        this.amount=amount;
//        cloud = new double[amount]; //tu su ulozene vsetky vzdialenosti od toho ktoreho bodu
    }
    
    /**
     * vypocet metriky pre kazdy bod
     * @return  0 zero
     */
    int getPoint(){
        // Create input- and output data 
        int n = amount;
//        Double dist_last = Double.MAX_VALUE;
        Float dist_last = Float.MAX_VALUE;
        float srcArrayX[] = new float[n]; //x
        float srcArrayY[] = new float[n]; //y
        float srcArrayZ[] = new float[n]; //z
        float minArray[]  = new float[n];  //vysledok min
        float avgArray[]  = new float[n];  //vysledok avg
        
        for (int i = 0; i < point_cloud1.size(); i++) {
            srcArrayX[i] = (float) point_cloud1.get(i).getX();
            srcArrayY[i] = (float) point_cloud1.get(i).getY();
            srcArrayZ[i] = (float) point_cloud1.get(i).getZ();
        }
        
        Pointer srcX = Pointer.to(srcArrayX);
        Pointer srcY = Pointer.to(srcArrayY);
        Pointer srcZ = Pointer.to(srcArrayZ);
        Pointer min = Pointer.to(minArray);
        Pointer avg = Pointer.to(avgArray);

        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        
        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device}, 
            null, null, null);
        
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = 
            clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[5];
        memObjects[0] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcX, null);
        memObjects[1] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcY, null);
        memObjects[2] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcZ, null);
        memObjects[3] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * n, null, null);
        memObjects[4] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * n, null, null);
        
        // Program Setup - The source code of the OpenCL program to execute
        String programSource = readFile("kernels/CLmetrika.cl");
        
        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);
        
        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "CLmetrika", null);
        
        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(memObjects[4]));
        clSetKernelArg(kernel, 5, Sizeof.cl_int, Pointer.to(new int[]{n}));
        clSetKernelArg(kernel, 6, Sizeof.cl_float, Pointer.to( new float[]{dist_last} ));
        
        
        // Set the work-item dimensions
        long global_work_size[] = new long[]{n};
        long local_work_size[] = new long[]{1};
        
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);
        
        // Read the output data MIN
        clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
            n * Sizeof.cl_float, min, 0, null, null);
        // Read the output data AVG
        clEnqueueReadBuffer(commandQueue, memObjects[4], CL_TRUE, 0,
            n * Sizeof.cl_float, avg, 0, null, null);
        
        // Release kernel, program, and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);
        clReleaseMemObject(memObjects[3]);
        clReleaseMemObject(memObjects[4]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
        
        // the result
        // priradime do mracna kazdemu bodu jeho metriku ! :-)
        for (int i = 0; i < point_cloud1.size(); i++) {
            point_cloud1.get(i).setMin(minArray[i]);
            point_cloud1.get(i).setAvg(avgArray[i]);
            System.out.println(i + " min/avg " + minArray[i] + " / " + avgArray[i]);
        }

        return 0;
    }
    
    
/**
 * SORT
 */
    ArrayList<Point> /*Point[]*/ sort() {
        Collections.sort(point_cloud1);
        return point_cloud1;
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
        
        return  Math.sqrt((double) (dx * dx + dy * dy + dz * dz));
    }

    /**
     * zaokruhlenie na pozadovany pocet des miest
     * @param Rval
     * @param Rpl
     * @return
     */
    private Double round(Double Rval, int Rpl) {
        double p = Math.pow(10, Rpl);
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return  tmp / p;
    }

/**
     * Helper function which reads the file with the given name and returns 
     * the contents of this file as a String. Will exit the application
     * if the file can not be read.
     * 
     * @param fileName The name of the file to read.
     * @return The contents of the file
     */
    private String readFile(String fileName){
        try
        {
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName)));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while (true)
            {
                line = br.readLine();
                if (line == null)
                {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}

