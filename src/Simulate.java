import simulator.Kernel;
import simulator.Config;
import simulator.Profiling;
import simulator.TRACE;
//
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


import java.util.Scanner;
/**
 * Main Simulator program
 *
 * @author Stephan Jamieson
 * @version 25/4/2022
 */
public class Simulate {

    private Simulate() {}
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        //System.out.println("*** Simulator ***");
        final Scanner scanner = new Scanner(System.in);
        final String configFileName;
        String kernelName;
        final String kernelParameters;
        final int sysCallCost;
        final int dispatchCost;
        final int traceLevel;
        final String outputFilename;
        
      
        
        //final String runFileName = scanner.nextLine().trim();
        final String runFileName = args[0];
        if (!runFileName.equals("")) {
            //System.out.println("Reading run data from file.");
            final File input_file = new File(runFileName);
            final BufferedReader reader = new BufferedReader(new FileReader(input_file));
            
            configFileName = reader.readLine().trim();
            kernelName = reader.readLine().trim();
            kernelParameters = reader.readLine().trim();
            sysCallCost = Integer.parseInt(reader.readLine().trim());
            dispatchCost = Integer.parseInt(reader.readLine().trim());
            outputFilename = reader.readLine().trim();
        }
        else {
            //System.out.print("Configuration file name? ");
            configFileName = scanner.nextLine().trim();

            //System.out.print("Kernel name? ");
            kernelName = scanner.nextLine().trim();
            //System.out.print("Enter kernel parameters (if any) as a comma-separated list: ");
            kernelParameters = scanner.nextLine().trim();

            //System.out.print("Cost of system call? ");
            sysCallCost = scanner.nextInt();

            //System.out.print("Cost of context switch: ");
            dispatchCost = scanner.nextInt();

            //System.out.print("Trace level (0-31)? ");
            traceLevel = scanner.nextInt();
            TRACE.SET_TRACE_LEVEL(traceLevel);
            
            //scanner.nextLine();
            //System.out.println("Write execution profile to CSV? Enter a file name or press return: ");
            outputFilename = scanner.nextLine().trim();


            }
        //System.out.println("Instantiating kernel with supplied parameters...");
        final Kernel kernel;       
        try {
            if (kernelName.endsWith(".class") || kernelName.endsWith(".java")) {
                kernelName = kernelName.substring(kernelName.length()-5);
            }

            final Class<?> target = Simulate.class.getClassLoader().loadClass(kernelName); // , Simulate.class.getClassLoader()); 
            kernel = (Kernel)target.getConstructor(Object[].class).newInstance((Object)kernelParameters.split("\\s*,\\s*"));   
        }
        catch (ClassNotFoundException classNotFound) {
            System.out.println("Unable to load kernel class. Wrong name or not compiled?");
            return ;
        }
        catch (NoSuchMethodException noSuchConstruct) {
            System.out.println(noSuchConstruct);
            System.out.println("Unable to locate suitable constructor.\n");
            return;
        } 
        catch (Exception excep) {
            System.out.println("Unable to instantiate kernel");
            excep.printStackTrace();
            return;
        }
        
        //System.out.println("Building configuration...");
        Config.init(kernel, dispatchCost, sysCallCost);
        try {
            Config.buildConfiguration(configFileName);
        }
        catch (Config.ConfigurationBuildException confBuildExcep) {
            System.out.println("An error occured attempting to process the configuration file.");
            System.out.println(confBuildExcep.getMessage());
            System.exit(-1);
        }
        catch (FileNotFoundException fnfExcep) {
            System.out.println("Unable to open configuration file. Not found.");
            System.exit(-1);
        }
        catch (IOException ioExcep) {
            System.out.println("An I/O error occured when processing the configuration file.");
            System.exit(-1);
        }
        //System.out.println("Running simulation...");
        Config.run();
        //System.out.println("Done");
        //System.out.println(Config.getSystemTimer());
        //System.out.println("Context switches: "+Config.getCPU().getContextSwitches());

        // Just get paramaters of file - only works with my level of file hierarchy
        int indexSlash = runFileName.lastIndexOf('/');
        int indexFirstSlash = runFileName.indexOf('/');
        int indexDot = runFileName.lastIndexOf('.');
        String changing = runFileName.substring(indexFirstSlash+1, indexSlash);
        String name = runFileName.substring(indexSlash+1, indexDot);

        System.out.printf("%s %s : %.2f\n", name, changing, ((double)Config.getSystemTimer().getUserTime())/Config.getSystemTimer().getSystemTime()*100);
        
        try {
            if (!outputFilename.equals("")) {
                //System.out.println("Output to file: "+outputFilename);
                Profiling.writeCSV(outputFilename);
            } else {System.out.println("No output file specified.");}
        }
        catch (IOException ioExcep) {
            System.out.println("Unable to write CSV file.");
            ioExcep.printStackTrace();
            
        }
    }


    private static Kernel instantiateKernel(final String kernelName, final String kernelParameters) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Class<?> target = Simulate.class.getClassLoader().loadClass(kernelName); // , Simulate.class.getClassLoader());
        if (kernelParameters.equals("")) {
            return (Kernel)target.getConstructor().newInstance();
        }
        else {
            return (Kernel)target.getConstructor(String[].class).newInstance((Object)kernelParameters.split("\\s*,\\s*"));   
        }
    }
}
