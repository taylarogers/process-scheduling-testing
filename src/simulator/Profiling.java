package simulator;

import simulator.Config;
import simulator.SystemTimer;
import simulator.ProcessControlBlock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Record of time spent in each process state.
 *
 * @author Stephan Jamieson
 * @version 14/4/2022
 */
public class Profiling {

    private static Map<Integer, Profile> profiles = new HashMap<Integer, Profile>();
    //private static List<Interval> intervals = new ArrayList<Interval>();
    
    private static CPU.Mode currentMode;
    
    
    static void recordTransition(final CPU cpu) {
        Profiling.currentMode = cpu.getMode();
        if (!cpu.isIdle()) {
            final ProcessControlBlock currentProcess = cpu.getCurrentProcess();
            profiles.get(currentProcess.getPID()).recordTransition(Config.getSystemTimer().getSystemTime(), Profiling.currentMode);
        }
    }

    static void recordTransition(final ProcessControlBlock pcb) {
        profiles.get(pcb.getPID()).recordTransition(Config.getSystemTimer().getSystemTime(), pcb.getState());        
    }

    /**
     * Obtain a profiler for the target process.
     */
    static void createProfile(final ProcessControlBlock target) {
        ///*DEBUG*/System.out.printf("createProfile(%s)\n", target.toString());
        final long currentTime = Config.getSystemTimer().getSystemTime();
        final Profile profile = new Profile(currentTime, target.getPID(), target.getProgramName(), target.getState(), Profiling.currentMode);
        Profiling.profiles.put(profile.getPID(), profile);
    }
    
    
    /**
     * Write a CSV file for all processes containing process states, start and finish times.
     */
    public static void writeCSV(final String filename) throws IOException {
        final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename));
        bufferedWriter.write(Interval.HEADER);
        bufferedWriter.newLine();
        for(Profile profile : profiles.values()) {
    
            for (Interval interval : profile.getIntervals()) {
                bufferedWriter.write(interval.toString());
                bufferedWriter.newLine();            
            }
        }
        bufferedWriter.close();
    }
    
    
    private static class Interval {
        final int PID;
        final String programName;
        final ProcessControlBlock.State state;
        final CPU.Mode mode;
        final Long startTime;
        final Long endTime;
        Interval(final int PID, final String programName, final Transition previous, final Transition current) {
            this(PID, programName, previous.state, previous.mode, previous.timeStamp, current.timeStamp);
        }
        Interval(final int PID, final String programName, final ProcessControlBlock.State state, final CPU.Mode mode, final Long startTime, final Long endTime) {
            this.PID = PID;
            this.programName = programName;
            this.state = state;
            this.mode = mode;
            this.startTime = startTime;
            this.endTime = endTime;
        }
        public long duration() { return endTime-startTime; }
        
        public String toString() {
            final String mode = (this.state == ProcessControlBlock.State.WAITING || this.state == ProcessControlBlock.State.READY
                || this.state == ProcessControlBlock.State.TERMINATED) ? "N/A" : this.mode.toString();
            final String endTime = this.endTime == null ? "-" : String.format("%010d", this.endTime.longValue());
            return String.format("%03d, %s, %s, %010d, %s, %s", this.PID, this.state, mode, this.startTime, endTime, this.programName); 
        }
        public final static String HEADER = "PID, STATE, MODE, START TIME, END TIME, PROGRAM";
    }
    
    private static class Transition {
        private final Long timeStamp;
        private final ProcessControlBlock.State state;
        private final CPU.Mode mode;
        public Transition(final Long timeStamp, final ProcessControlBlock.State state, final CPU.Mode mode) {
            this.timeStamp = timeStamp;
            this.state = state;
            this.mode = mode;
        }
        public boolean equivalentTo(Transition other) {
            return this.state == other.state && this.mode == other.mode;
        }
        
        public String toString() {
            return String.format("%d, %s, %s", timeStamp, state, mode);
        }
    }
    
    
    static class Profile {

        private Transition previousTransition;
        private List<Interval> intervals;
        private final int PID;
        private final String programName;
        
        /**
         * Create a new profiler for the given target.
         * 
         * Requires target has initial state set.
         */
        Profile(final long timeStamp, final int PID, final String programName, final ProcessControlBlock.State state, CPU.Mode mode) {
            this.PID = PID;
            this.programName = programName;
            this.previousTransition = new Transition(timeStamp, state, mode);
            this.intervals = new ArrayList<Interval>();
            /*DEBUG System.out.printf("%d, %s\n", this.PID, this.previousTransition);*/

        }
    
        /**
         * Record that the target has transitioned from previousState to newState.
         */
        public void recordTransition(final long timeStamp, final ProcessControlBlock.State newState) {
            recordTransition(timeStamp, newState, previousTransition.mode);
        }  
        
        /**
         * Record that the target has transitioned into a different processor mode.
         */
        public void recordTransition(final long timeStamp, final CPU.Mode mode) {
            recordTransition(timeStamp, previousTransition.state, mode);
        }
        
        private void recordTransition(final long timeStamp, final ProcessControlBlock.State state, CPU.Mode mode) {
            final Transition currentTransition = new Transition(timeStamp, state, mode);
            Interval interval = new Interval(this.PID, this.programName, previousTransition, currentTransition);
            if (interval.duration()>0) {
                if (interval.state == ProcessControlBlock.State.READY && !intervals.isEmpty()) {
                    final Interval previous = intervals.get(intervals.size()-1);
                    if (previous.state == ProcessControlBlock.State.READY) {
                        intervals.remove(intervals.size()-1);
                        interval = new Interval(this.PID, this.programName, previous.state, previous.mode, previous.startTime, interval.endTime);   
                    }
                }
                this.intervals.add(interval);
            }
            /*DEBUG System.out.printf("%d, %s\n", this.PID, currentTransition); */
            previousTransition = currentTransition;
            
            if (state == ProcessControlBlock.State.TERMINATED) {
                final Transition terminal = new Transition(null, state, mode);
                this.intervals.add(new Interval(this.PID, this.programName, previousTransition, terminal));
            }
        }

        /**
         * Obtain PID of associated process.
         */
        public int getPID() {
            return this.PID;
        }
        
        public List<Interval> getIntervals() {
            return this.intervals;
        }
        
        public String getProgramName() { return this.programName; }
    }
}
