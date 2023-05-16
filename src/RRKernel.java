import simulator.Config;
import simulator.IODevice;
import simulator.Kernel;
import simulator.ProcessControlBlock;
//
import java.io.FileNotFoundException;
import java.io.IOException;
//
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Concrete Kernel type
 * 
 * @author Stephan Jamieson
 * @version 8/3/15
 */
public class RRKernel implements Kernel {
    

    private Deque<ProcessControlBlock> readyQueue;
    private int SLICE_TIME;
        
    public RRKernel(Object... varargs) {
        assert(varargs.length==1);
        this.readyQueue = new ArrayDeque<ProcessControlBlock>();
        this.SLICE_TIME=Integer.valueOf((String)varargs[0]);
        ///*DEBUG*/System.out.printf("Slice time %d.\n", this.SLICE_TIME);
    }
    
    private ProcessControlBlock dispatch() {
            ProcessControlBlock oldProc;
            if (!readyQueue.isEmpty()) {
                ProcessControlBlock nextProc = readyQueue.removeFirst();
                oldProc = Config.getCPU().contextSwitch(nextProc);
                nextProc.setState(ProcessControlBlock.State.RUNNING);
                Config.getSystemTimer().scheduleInterrupt(SLICE_TIME, this, nextProc.getPID());
            }
            else {
                oldProc = Config.getCPU().contextSwitch(null);
            }
            return oldProc;
    }
            
    
                
    public int syscall(int number, Object... varargs) {
        int result = 0;
        switch (number) {
             case MAKE_DEVICE:
                {
                    IODevice device = new IODevice((Integer)varargs[0], (String)varargs[1]);
                    Config.addDevice(device);
                }
                break;
                
             case EXECVE: 
                {                    
                    ProcessControlBlock pcb = this.loadProgram((String)varargs[0]);
                    if (pcb!=null) {
                        // Loaded successfully.
                        pcb.setPriority((Integer)varargs[1]);
                        readyQueue.addLast(pcb);
                        if (Config.getCPU().isIdle()) { this.dispatch(); }
                    }
                    else {
                        result = -1;
                    }
                }
                break;
                
             case IO_REQUEST: 
                {
                    ProcessControlBlock ioRequester = Config.getCPU().getCurrentProcess();
                    Config.getSystemTimer().cancelInterrupt(Config.getCPU().getCurrentProcess().getPID());
                    IODevice device = Config.getDevice((Integer)varargs[0]);
                    device.requestIO((Integer)varargs[1], ioRequester, this);
                    ioRequester.setState(ProcessControlBlock.State.WAITING);
                    dispatch();
                }
                break;
                
             case TERMINATE_PROCESS:
                {
                    Config.getCPU().getCurrentProcess().setState(ProcessControlBlock.State.TERMINATED);
                    Config.getSystemTimer().cancelInterrupt(Config.getCPU().getCurrentProcess().getPID());    
                    dispatch();
                }
                break;
             default:
                result = -1;
        }
        return result;
    }
   
    
    public void interrupt(int interruptType, Object... varargs){
        switch (interruptType) {            
            case TIME_OUT:
                {
                    int processID = (Integer)varargs[0];
                    assert(Config.getCPU().getCurrentProcess()!=null && processID==Config.getCPU().getCurrentProcess().getPID());
                    if (readyQueue.isEmpty()) {
                        // Given current process another slice.
                        Config.getSystemTimer().scheduleInterrupt(SLICE_TIME, this, processID);
    
                    }
                    else {
                        Config.getCPU().getCurrentProcess().setState(ProcessControlBlock.State.READY);
                        ProcessControlBlock oldProc = dispatch();
                        readyQueue.addLast(oldProc);
                    }
                }
                break;
            case WAKE_UP:
                {
                    ProcessControlBlock process = (ProcessControlBlock)varargs[1]; 
                    process.setState(ProcessControlBlock.State.READY);
                    readyQueue.addLast(process);
                    if (Config.getCPU().isIdle()) { 
                        this.dispatch(); 
                    }
                }
                break;
                
            default:
                throw new IllegalArgumentException("RoundRobinKernel:interrupt("+interruptType+"...): unknown type.");
        }
    }
    
    private static ProcessControlBlock loadProgram(String filename) {
        try {
            return ProcessControlBlock.loadProgram(filename);
        }
        catch (FileNotFoundException fileExp) {
            throw new IllegalArgumentException("RoundRobinKernel: loadProgram(\""+filename+"\"): file not found.");
        }
        catch (IOException ioExp) {
            throw new IllegalArgumentException("RoundRobinKernel: loadProgram(\""+filename+"\"): IO error.");
        }
    }
    
    private void dumpReadyQueue() {
        System.out.println("Dumping ready queue");
        for(ProcessControlBlock pcb : readyQueue) {
             System.out.println(pcb);   
        }
    }
}
