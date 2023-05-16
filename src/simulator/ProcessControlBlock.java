package simulator;
import simulator.Config;
import simulator.CPUInstruction;
import simulator.Instruction;
import simulator.IOInstruction;
import simulator.Profiling;
import simulator.SystemTimer;
//
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Scanner;
/**
 * Implementation of abstract ProcessControlBlock type.
 * 
 * @author Stephan Jamieson
 * @version 13/3/15
 */
public class ProcessControlBlock {

    /**
     * Possible process states.
     */
    public enum State { WAITING, READY, RUNNING, TERMINATED };

    private State state;
    private int ID;
    private int priority;
    private String programName;
    private Iterator<Instruction> instructions;
    private Instruction current;
    
    
    private ProcessControlBlock(int ID, String programName, List<Instruction> instructions) {
        this.ID = ID;
        this.programName = programName;
        this.priority=0;
        this.instructions = instructions.iterator();
        assert(this.instructions.hasNext());
        this.current = this.instructions.next(); 
        this.state = State.READY;
        Profiling.createProfile(this);
    }

    public int getPID() { return ID; }
    public String getProgramName() { return programName; }
    public int getPriority() { return this.priority; }
    public int setPriority(int value) {
        final int old = getPriority();
        this.priority=value;
        return old;
    }
    public Instruction getInstruction() { return current; }
    public boolean hasNextInstruction() { return instructions.hasNext(); }
    public void nextInstruction() { assert(this.hasNextInstruction()); current = instructions.next(); }
    public State getState() { return state; }
    public void setState(State state) { 
        this.state = state; 
        Profiling.recordTransition(this);
    }
    private static int nextID = 1;
    private static int makeID() {
        nextID++;
        return nextID-1;
    }
    
    public static ProcessControlBlock loadProgram(String filename) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<Instruction> instructions = new ArrayList<Instruction>();
        
        String line = reader.readLine();
        while (line!=null) {
            if (line.trim().startsWith("#")) {
                // It's a comment, ignore.
            }
            else {
                Scanner scanner = new Scanner(line);
                String token;
                int duration;
                if (!scanner.hasNext()) {
                    throw new IOException("ProcessControlBlockImpl:loadProgram("+filename+"): illegal line in file. Missing token.");
                }
                else {
                    token = scanner.next();
                }
                if (!scanner.hasNextInt()) {
                    throw new IOException("ProcessControlBlockImpl:loadProgram("+filename+"): illegal line in file. Missing duration.");
                }
                else {
                    duration = scanner.nextInt();
                }
                if (token.equals("CPU")) {
                    instructions.add(new CPUInstruction(duration));
                }
                else if (token.equals("IO")) {
                    if (!scanner.hasNextInt()) {
                        throw new IOException("ProcessControlBlockImpl:loadProgram("+filename+"): illegal I/O line in file. Missing device ID.");
                    }
                    else {
                        int deviceID = scanner.nextInt();
                        instructions.add(new IOInstruction(duration, deviceID));
                    }
                }
                else {
                    throw new IOException("ProcessControlBlockImpl:loadProgram("+filename+"): illegal token in file.");
                }
            }
            line = reader.readLine();
        }
        reader.close();
        
        return new ProcessControlBlock(ProcessControlBlock.makeID(), filename, instructions);
    }
    
    public String toString() {
        return String.format("process(pid=%d, state=%s, name=\"%s\")", this.getPID(), this.getState(), this.getProgramName());
    }
}
