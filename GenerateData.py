def GenerateFCFSContextSwitch(sysCallCost, startingContextSwitch, interval):
    for newCS in range(startingContextSwitch, startingContextSwitch + 46, interval):
        filename = "ContextSwitch" + "/" + "FCFS_" + str(sysCallCost) + "_" + str(newCS)
        file = open("data/" + filename + ".inp", "w")
        file.write('workload/workloadConfig.cfg\nFCFSKernel\n\n')
        file.write(str(sysCallCost) + "\n" + str(newCS) + "\n")
        file.write("output/" + filename + ".txt\n")
        file.close()

def GenerateRRContextSwitch(sysCallCost, timeQuantum, startingContextSwitch, interval):
    for newCS in range(startingContextSwitch, startingContextSwitch + 46, interval):
        filename = "ContextSwitch" + "/" + "RR_" + str(timeQuantum) + "_" + str(sysCallCost) + "_" + str(newCS)
        file = open("data/" + filename + ".inp", "w")
        file.write('workload/workloadConfig.cfg\nRRKernel\n')
        file.write(str(timeQuantum) + "\n" + str(sysCallCost) + "\n" + str(newCS) + "\n")
        file.write("output/" + filename + ".txt\n")
        file.close()

def GenerateRRTimeQuantum(sysCallCost, contextSwitchCost, startingTimeQuantum, interval):
    for newTQ in range(startingTimeQuantum, startingTimeQuantum + 1101, interval):
        filename = "TimeQuantum" + "/" + "RR_" + str(newTQ) + "_" + str(sysCallCost) + "_" + str(contextSwitchCost)
        file = open("data/" + filename + ".inp", "w")
        file.write('workload/workloadConfig.cfg\nRRKernel\n')
        file.write(str(newTQ) + "\n" + str(sysCallCost) + "\n" + str(contextSwitchCost) + "\n")
        file.write("output/" + filename + ".txt\n")
        file.close()

# Generate context switching files, 3 different quanta for RR
GenerateFCFSContextSwitch(4, 5, 5)
GenerateRRContextSwitch(4, 50, 5, 5)
GenerateRRContextSwitch(4, 300, 5, 5)
GenerateRRContextSwitch(4, 700, 5, 5)

# Generate time quanta files, 3 different context switching times
GenerateRRTimeQuantum(4, 5, 100, 100)
GenerateRRTimeQuantum(4, 20, 100, 100)
GenerateRRTimeQuantum(4, 35, 100, 100)