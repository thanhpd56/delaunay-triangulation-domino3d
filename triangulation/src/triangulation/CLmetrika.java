/*
 * Vypocet metriky pre body pomocou GPGPU
 * previazane pouzitim JOCL - Java bindings for OpenCL
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
 * Vypocet metriky pre body pomocou GPGPU
 * previazane pouzitim JOCL - Java bindings for OpenCL
 * @author Domino
 */
public class CLmetrika
{    
    private int amount;
    ArrayList<Point> point_cloud1;
    private long time;



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
        int m = amount/10000; //not used
        
//        Double dist_last = Double.MAX_VALUE;
        Float dist_last = Float.MAX_VALUE;
        float srcArrayX[] = new float[n]; //x
        float srcArrayY[] = new float[n]; //y
        float srcArrayZ[] = new float[n]; //z
//        float minArray[]  = new float[n];  //vysledok min
//        float avgArray[]  = new float[n];  //vysledok avg 
        float minArray[]  = new float[10000];  //vysledok min
        float avgArray[]  = new float[10000];  //vysledok avg 
        
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
//        memObjects[3] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * n, null, null);
//        memObjects[4] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * n, null, null);
        memObjects[3] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * 10000, null, null);
        memObjects[4] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * 10000, null, null);
        
        // Program Setup - The source code of the OpenCL program to execute
        String programSource = readFile("kernels/CLmetrika.cl");
        
        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        
System.out.println("building metrika kernel...");
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);
System.out.println("...done");

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "CLmetrika", null);

        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(memObjects[4]));
        clSetKernelArg(kernel, 5, Sizeof.cl_int, Pointer.to(new int[]{n}));
//        clSetKernelArg(kernel, 6, Sizeof.cl_int, Pointer.to(new int[]{m}));
        clSetKernelArg(kernel, 7, Sizeof.cl_float, Pointer.to( new float[]{dist_last} ));

        
        // Set the work-item dimensions
        long global_work_size[] = new long[]{10000};
//        long local_work_size[] = new long[]{10}; //moze byt aj null ,potom sa sam rozhodne
        long local_work_size[] = null; //moze byt aj null ,potom sa sam rozhodne
        

        long start = System.currentTimeMillis();

        for (int j = 0; j <= m; j++) {
            
            clSetKernelArg(kernel, 6, Sizeof.cl_int, Pointer.to(new int[]{j}));
            
            System.out.println("executing metrika kernel");
            // Execute the kernel
            clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                    global_work_size, local_work_size, 0, null, null);
            System.out.println("stop metrika kernel");
            // Read the output data 
            int clEnqueueReadBuffer = clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
                    10000 * Sizeof.cl_float, min, 0, null, null);
            System.out.println("1read metrika kernel +err:" + clEnqueueReadBuffer);
            
            int clEnqueueReadBuffer1 = clEnqueueReadBuffer(commandQueue, memObjects[4], CL_TRUE, 0,
                    10000 * Sizeof.cl_float, avg, 0, null, null);
            System.out.println("2read metrika kernel +err:" + clEnqueueReadBuffer1);
            
            for (int i = j*10000; i < (j+1)*10000 && i < point_cloud1.size(); i++) {
                point_cloud1.get(i).setMin(minArray[i-j*10000]);
                point_cloud1.get(i).setAvg(avgArray[i-j*10000]);
                System.out.println(i+" "+ (i-j*10000) + " min/avg " + minArray[i-j*10000] + " / " + avgArray[i-j*10000]);
            }
            
        }
        clFinish(commandQueue);
        long end = System.currentTimeMillis();
        time = end - start;


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
System.out.println("result metrika kernel");
        // the result
        // priradime do mracna kazdemu bodu jeho metriku ! :-)
//        for (int i = 0; i < point_cloud1.size(); i++) {
//            point_cloud1.get(i).setMin(minArray[i]);
//            point_cloud1.get(i).setAvg(avgArray[i]);
//            System.out.println(i + " min/avg " + minArray[i] + " / " + avgArray[i]);
//        }
System.out.println("return metrika kernel");
        return 0;
    }
    
    
/**
 * SORT
 */
    ArrayList<Point> /*Point[]*/ sort() {
//        Collections.sort(point_cloud1);
        return point_cloud1;
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

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }
}

