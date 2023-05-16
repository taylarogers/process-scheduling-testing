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
public class FCFSKernel implements Kernel {
    

    private Deque<ProcessControlBlock> readyQueue;
        
    public FCFSKernel(Object... varargs) {
        this.readyQueue = new ArrayDeque<ProcessControlBlock>();
    }
    
    private ProcessControlBlock dispatch() {
            ProcessControlBlock oldProc;
            if (!readyQueue.isEmpty()) {
                ProcessControlBlock nextProc = readyQueue.removeFirst();
                oldProc = Config.getCPU().contextSwitch(nextProc);
                nextProc.setState(ProcessControlBlock.State.RUNNING);
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
                    IODevice device = Config.getDevice((Integer)varargs[0]);
                    device.requestIO((Integer)varargs[1], ioRequester, this);
                    ioRequester.setState(ProcessControlBlock.State.WAITING);
                    dispatch();
                }
                break;
             case TERMINATE_PROCESS:
                {
                    Config.getCPU().getCurrentProcess().setState(ProcessControlBlock.State.TERMINATED);
                    ProcessControlBlock process = dispatch();
                    //process.setState(ProcessControlBlock.State.TERMINATED);
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
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not support timeouts.");
            case WAKE_UP:
                ProcessControlBlock process = (ProcessControlBlock)varargs[1]; 
                process.setState(ProcessControlBlock.State.READY);
                readyQueue.addLast(process);
                if (Config.getCPU().isIdle()) { this.dispatch(); }
                break;
            default:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): unknown type.");
        }
    }
    
    private static ProcessControlBlock loadProgram(String filename) {
        try {
            return ProcessControlBlock.loadProgram(filename);
        }
        catch (FileNotFoundException fileExp) {
            throw new IllegalArgumentException("FCFSKernel: loadProgram(\""+filename+"\"): file not found.");
        }
        catch (IOException ioExp) {
            throw new IllegalArgumentException("FCFSKernel: loadProgram(\""+filename+"\"): IO error.");
        }
    }
}
