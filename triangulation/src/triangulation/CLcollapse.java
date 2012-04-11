/*
 * Vypocet spojenia bodov pomocou GPGPU
 * previazane pouzitim JOCL - Java bindings for OpenCL
 */
package triangulation;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.*;
import static org.jocl.CL.*;
import org.jocl.*;
import com.sun.org.apache.bcel.internal.generic.FLOAD;
import java.util.Arrays;
import java.util.Collections;



/**
 * Vypocet spojenia bodov pomocou GPGPU
 * previazane pouzitim JOCL - Java bindings for OpenCL
 * @author Domino
 */
class CLcollapse extends ArrayList {

    ArrayList<Point> returnArray = new ArrayList<Point>();
    private long time;
    
    CLcollapse(
            float tolerance, 
            ArrayList<Point> point_cloud, 
            int amount) {
                // Create input- and output data 
        Point[] ppp = new Point[amount];
        
        int n = amount;
        float srcArrayX[] = new float[n]; //x
        float srcArrayY[] = new float[n]; //y
        float srcArrayZ[] = new float[n]; //z
        float colXArray[] = new float[n];  //vysledok po collapse x
        float colYArray[] = new float[n];  //y
        float colZArray[] = new float[n];  //z
        int   valid[]     = new int[n];
        
        System.out.println("a>>>>" + point_cloud.size() + "<<<" + point_cloud.toString());
        
        for (int i = 0; i < point_cloud.size(); i++) {
            srcArrayX[i] = (float) point_cloud.get(i).getX();
            srcArrayY[i] = (float) point_cloud.get(i).getY();
            srcArrayZ[i] = (float) point_cloud.get(i).getZ();
        }
        
        Pointer srcX = Pointer.to(srcArrayX);
        Pointer srcY = Pointer.to(srcArrayY);
        Pointer srcZ = Pointer.to(srcArrayZ);
        Pointer colX = Pointer.to(colXArray);
        Pointer colY = Pointer.to(colYArray);
        Pointer colZ = Pointer.to(colZArray);
        Pointer val  = Pointer.to(valid);

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
        cl_mem memObjects[] = new cl_mem[7];
        memObjects[0] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcX, null);
        memObjects[1] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcY, null);
        memObjects[2] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcZ, null);
        memObjects[3] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * n, null, null);
        memObjects[4] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * n, null, null);
        memObjects[5] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * n, null, null);
        memObjects[6] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,  Sizeof.cl_int * n, val, null);
        
        // Program Setup - The source code of the OpenCL program to execute
        String programSource = readFile("kernels/CLcollapse.cl");
        
        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);
        
        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "CLcollapse", null);
        
        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(memObjects[4]));
        clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(memObjects[5]));
        clSetKernelArg(kernel, 6, Sizeof.cl_int, Pointer.to(new int[]{n}));
        clSetKernelArg(kernel, 7, Sizeof.cl_float, Pointer.to( new float[]{tolerance} ));
        clSetKernelArg(kernel, 8, Sizeof.cl_mem, Pointer.to(memObjects[6]));
        
        
        // Set the work-item dimensions
        long global_work_size[] = new long[]{n};
        long local_work_size[] = new long[]{1};
        
        
        clFinish(commandQueue);
        long start = System.currentTimeMillis();
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);
        clFinish(commandQueue);
        long end = System.currentTimeMillis();
        time = end - start;
        
        
        
        // Read the output data colapsed X
        clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
            n * Sizeof.cl_float, colX, 0, null, null);
        // Read the output data Y
        clEnqueueReadBuffer(commandQueue, memObjects[4], CL_TRUE, 0,
            n * Sizeof.cl_float, colY, 0, null, null);
        // Read the output data Z
        clEnqueueReadBuffer(commandQueue, memObjects[5], CL_TRUE, 0,
            n * Sizeof.cl_float, colZ, 0, null, null);
        // Read the output data VALID
        clEnqueueReadBuffer(commandQueue, memObjects[6], CL_TRUE, 0,
            n * Sizeof.cl_int, val, 0, null, null);
        
        // Release kernel, program, and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);
        clReleaseMemObject(memObjects[3]);
        clReleaseMemObject(memObjects[4]);
        clReleaseMemObject(memObjects[5]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
        
        // the result
        // priradime do mracna kazdemu bodu jeho nove xyz suradnice
        int j = 0; 
        //prepisanie ciselID bodov, kedze mohli vzniknut viacere s rovn. cislomID
        for (int i = 0; i < point_cloud.size(); i++) {
            if (valid[i] == 1) {
                returnArray.add(new Point(j++, 
                        colXArray[i], 
                        colYArray[i], 
                        colZArray[i]));
//                System.out.println(""+returnArray.get(i).toString());
            }
        }

        System.out.println("b>>>>" + point_cloud.size() + "X"+j + "<<<" + point_cloud.toString());
        //end
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
     * @return the returnArray
     */
    public ArrayList<Point> getReturnArray() {
        return returnArray;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }
}


