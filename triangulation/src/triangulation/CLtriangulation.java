/*
 * Vypocet triangulacie pomocou GPGPU
 * previazane pouzitim JOCL - Java bindings for OpenCL
 */
package triangulation;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.*;
import static org.jocl.CL.*;
import org.jocl.*;
import com.sun.org.apache.bcel.internal.generic.FLOAD;
import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;

/**
 *  Vypocet triangulacie pomocou GPGPU
 * previazane pouzitim JOCL - Java bindings for OpenCL
 * @author Domino
 */
class CLtriangulation {

    private ArrayList<Edge> edges = new ArrayList<Edge>();
    private ArrayList<Face> face = new ArrayList<Face>();
    private long time;
    private String dev;
    
//    public CLtriangulation(ArrayList<Edge> edges, 
//            ArrayList<Face> face, 
//            ArrayList<Point> point_cloud, 
//            int amount) {
    public CLtriangulation(
            int[] firstTriangle,
            ArrayList<Point> point_cloud, 
            int amount) {
                // Create input- and output data 
        
        Float dist_last = Float.MAX_VALUE;
        int n = amount;
        int id[] =  new int [2];
        id[0] = 3;
        id[1] = 0;
        int idee[] = {0};
        int idff[] = {0};
        float srcArrayX[] = new float[n]; //x
        float srcArrayY[] = new float[n]; //y
        float srcArrayZ[] = new float[n]; //z
        float metrika[] = new float[n]; //metrika

        int   validPoint[]  = new int[n];
        for (int i = 0; i < n; i++) {
            validPoint[i] = 1;
        }
        
        int   edgeL[]  = new int[3*3*n]; //podla The Euler-Poincaré Formula 
        int   edgeR[]  = new int[3*3*n]; //podla The Euler-Poincaré Formula 
        int   validEdge[]  = new int[3*3*n]; //podla The Euler-Poincaré Formula 
        for (int i = 0; i < 3*3*n; i++) {
            validEdge[i] = 0;
            edgeL[i] = -1;
            edgeR[i] = -1;
        }
//??? potrebuje xyz/alebo stale pocitat stred  -->>  int   edgeMid[]= new int[3*n]; //podla The Euler-Poincaré Formula 
        int   faceV1[] = new int[7*n]; //Vert:Face:Edge 1:2:3 -> The Euler-Poincaré Formula 
        int   faceV2[] = new int[7*n]; //Vert:Face:Edge 1:2:3 -> The Euler-Poincaré Formula 
        int   faceV3[] = new int[7*n]; //Vert:Face:Edge 1:2:3 -> The Euler-Poincaré Formula 
        for (int i = 0; i < 7*n; i++) {
            faceV1[i] = -1;
            faceV2[i] = -1;
            faceV3[i] = -1;
        }
        
        //musim zapisat existujuci 1. 3uh z danych 3 bodov do polí
        validPoint[firstTriangle[0]] = -1;
        validPoint[firstTriangle[1]] = -1; //set used/invalid
        validPoint[firstTriangle[2]] = -1;
        
        edgeL[0] = firstTriangle[0];
        edgeR[0] = firstTriangle[1];
        validEdge[0] = 1;
        edgeL[1] = firstTriangle[0];
        edgeR[1] = firstTriangle[2];
        validEdge[1] = 1;
        edgeL[2] = firstTriangle[1];
        edgeR[2] = firstTriangle[2];
        validEdge[2] = 1;
        //musim zapisat existujuci 2. 3uh z danych 3 bodov do polí
        validPoint[firstTriangle[3]] = -1;
        validPoint[firstTriangle[4]] = -1; //set used/invalid
        validPoint[firstTriangle[5]] = -1;
        
        edgeL[3] = firstTriangle[3];
        edgeR[3] = firstTriangle[4];
        validEdge[3] = 1;
        edgeL[4] = firstTriangle[3];
        edgeR[4] = firstTriangle[5];
        validEdge[4] = 1;
        edgeL[5] = firstTriangle[4];
        edgeR[5] = firstTriangle[5];
        validEdge[5] = 1;
        //musim zapisat existujuci 3. 3uh z danych 3 bodov do polí
        validPoint[firstTriangle[6]] = -1;
        validPoint[firstTriangle[7]] = -1; //set used/invalid
        validPoint[firstTriangle[8]] = -1;
        
        edgeL[6] = firstTriangle[6];
        edgeR[6] = firstTriangle[7];
        validEdge[6] = 1;
        edgeL[7] = firstTriangle[6];
        edgeR[7] = firstTriangle[8];
        validEdge[7] = 1;
        edgeL[8] = firstTriangle[7];
        edgeR[8] = firstTriangle[8];
        validEdge[8] = 1;
        
        //todo: nezabudnut na konci pripocitat do arrayu face nakoniec prvy 3uh
        //...
        
        
        
        for (int i = 0; i < point_cloud.size(); i++) {
            srcArrayX[i] = (float) point_cloud.get(i).getX();
            srcArrayY[i] = (float) point_cloud.get(i).getY();
            srcArrayZ[i] = (float) point_cloud.get(i).getZ();
            metrika[i]   = (float) point_cloud.get(i).getMin();
        }
        
        Pointer srcX = Pointer.to(srcArrayX);
        Pointer srcY = Pointer.to(srcArrayY);
        Pointer srcZ = Pointer.to(srcArrayZ);
        Pointer min = Pointer.to(metrika);
        Pointer valP  = Pointer.to(validPoint);
        Pointer eL   = Pointer.to(edgeL);
        Pointer eR   = Pointer.to(edgeR);
        Pointer valE = Pointer.to(validEdge);
        Pointer fac1 = Pointer.to(faceV1);
        Pointer fac2 = Pointer.to(faceV2);
        Pointer fac3 = Pointer.to(faceV3);
        Pointer ids = Pointer.to(id);
        Pointer idE = Pointer.to(idee);
        Pointer idF = Pointer.to(idff);



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
        cl_mem memObjects[] = new cl_mem[14];
        memObjects[0] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcX, null);
        memObjects[1] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcY, null);
        memObjects[2] = clCreateBuffer(context,CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcZ, null);
        memObjects[3] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, min, null);
        memObjects[4] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,  Sizeof.cl_int * n, valP, null);
        memObjects[5] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,  Sizeof.cl_float * 3*3*n, eL, null);
        memObjects[6] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,  Sizeof.cl_float * 3*3*n, eR, null);
        memObjects[7] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,  Sizeof.cl_float * 3*3*n, valE, null);
        memObjects[8] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * 7*n, null, null);
        memObjects[9] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * 7*n, null, null);
        memObjects[10] = clCreateBuffer(context,CL_MEM_READ_WRITE,  Sizeof.cl_float * 7*n, null, null);
        memObjects[11] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,  Sizeof.cl_int * 2, ids, null);
        memObjects[12] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,  Sizeof.cl_int, idE, null);
        memObjects[13] = clCreateBuffer(context,CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,  Sizeof.cl_int, idF, null);

        
        // Program Setup - The source code of the OpenCL program to execute
        String programSource = readFile("kernels/CLtriangulation.cl");
        
        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);
        
        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "CLtriangulation", null);
        
        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(memObjects[4]));
        clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(memObjects[5]));
        clSetKernelArg(kernel, 6, Sizeof.cl_mem, Pointer.to(memObjects[6]));
        clSetKernelArg(kernel, 7, Sizeof.cl_mem, Pointer.to(memObjects[7]));
        clSetKernelArg(kernel, 8, Sizeof.cl_mem, Pointer.to(memObjects[8]));
        clSetKernelArg(kernel, 9, Sizeof.cl_mem, Pointer.to(memObjects[9]));
        clSetKernelArg(kernel, 10, Sizeof.cl_mem, Pointer.to(memObjects[10]));
        clSetKernelArg(kernel, 11, Sizeof.cl_int, Pointer.to(new int[]{n}));
//        clSetKernelArg(kernel, 7, Sizeof.cl_int, Pointer.to(firstTriangle));
        clSetKernelArg(kernel, 12,Sizeof.cl_float,Pointer.to( new float[]{dist_last} ));
        clSetKernelArg(kernel, 13,Sizeof.cl_mem,Pointer.to( memObjects[11]));
        clSetKernelArg(kernel, 14,Sizeof.cl_mem,Pointer.to( memObjects[12]));
        clSetKernelArg(kernel, 15,Sizeof.cl_mem,Pointer.to( memObjects[13]));
        
        
        // Set the work-item dimensions
        long global_work_size[] = new long[]{3*3*n};
//        long global_work_size[] = new long[]{1};
        long local_work_size[] = null; //1 grup sa zrobi, alebo null-spravi kolko on chce
//        long local_work_size[] = new long[]{1}; //1 grup sa zrobi, alebo null-spravi kolko chce





        clFinish(commandQueue);
        long start = System.currentTimeMillis();
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);

        clFinish(commandQueue);
        long end = System.currentTimeMillis();
        time = end - start;
        
        //  device name
        long size[] = new long[1];
        clGetDeviceInfo(devices[deviceIndex], CL_DEVICE_NAME, 0, null, size);
        byte bb[] = new byte[(int)size[0]];
        clGetDeviceInfo(devices[deviceIndex], CL_DEVICE_NAME, bb.length, Pointer.to(bb), null);
        String ss = new String(bb);
        dev = ss;
        
        
        // Read the output data
        int clEnqueueReadBuffer = clEnqueueReadBuffer(commandQueue, memObjects[5], CL_TRUE, 0,
                                      3*3*n * Sizeof.cl_float, eL, 0, null, null);
System.out.println("read 3 kernel +err:" + clEnqueueReadBuffer);
        clEnqueueReadBuffer(commandQueue, memObjects[6], CL_TRUE, 0,
            3*3*n * Sizeof.cl_float, eR, 0, null, null);
        
        clEnqueueReadBuffer(commandQueue, memObjects[7], CL_TRUE, 0,
            3*3*n * Sizeof.cl_float, valE, 0, null, null);
        
        clEnqueueReadBuffer(commandQueue, memObjects[8], CL_TRUE, 0,
            7*n * Sizeof.cl_float, fac1, 0, null, null);
        
        clEnqueueReadBuffer(commandQueue, memObjects[9], CL_TRUE, 0,
            7*n * Sizeof.cl_float, fac2, 0, null, null);
        
        clEnqueueReadBuffer(commandQueue, memObjects[10], CL_TRUE, 0,
            7*n * Sizeof.cl_float, fac3, 0, null, null);
        
        //read min
        clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
            n * Sizeof.cl_float, min, 0, null, null);
        
        
        
        // Release kernel, program, and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);
        clReleaseMemObject(memObjects[3]);
        clReleaseMemObject(memObjects[4]);
        clReleaseMemObject(memObjects[5]);
        clReleaseMemObject(memObjects[6]);
        clReleaseMemObject(memObjects[7]);
        clReleaseMemObject(memObjects[8]);
        clReleaseMemObject(memObjects[9]);
        clReleaseMemObject(memObjects[10]);
        clReleaseMemObject(memObjects[11]);
        clReleaseMemObject(memObjects[12]);
        clReleaseMemObject(memObjects[13]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
        
        for (int i = 0; i < 3*3*n; i++) {
System.out.println(">>"+edgeL[i]+" "+edgeR[i] +" "+ validEdge[i]);
        }
//        for (int i = 0; i < n; i++) {
//                System.out.println("XX"+metrika[i]);
//        }
        

        // the result
        for (int i = 0; i < 3*3*n; i++) {
//            if (validEdge[i] == 1) {
            if (edgeL[i] >= 0 && edgeR[i] >= 0) {
                edges.add(new Edge(
 /*p_c.get VELKE cislo, tu je chyba niekedy, neviem zatial preco*/                       
                        point_cloud.get(
                        edgeL[
                        i]), 
                        point_cloud.get(
                        edgeR[
                        i])));
            }
        }

        System.out.println(">>"+edges.toString());
        
        for (int i = 0; i < 7*n; i++) {
System.out.println("--"+faceV1[i]+" "+faceV2[i] +" "+ faceV3[i]);
            
            if (faceV1[i] != -1) {
                face.add(new Face(faceV1[i], faceV2[i], faceV3[i]));
            }
        }
        
System.out.println("##"+face.toString());

//        System.out.println("b>>>>" + point_cloud.size() + "X"+j + "<<<" + point_cloud.toString());
        //end
    }

    /**
     * @return the edges
     */
    public ArrayList<Edge> getEdges() {
        return edges;
    }
    /**
     * @return the face
     */
    public ArrayList<Face> getFace() {
        return face;
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
     * @return the time used for triangulation
     */
    public long getTime() {
        return time;
    }

    /**
     * @return the dev
     */
    public String getDev() {
        return dev;
    }
    
}
