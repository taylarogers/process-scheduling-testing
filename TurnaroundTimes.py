startTimes = []
endTimes = []
turnaroundTimes = []
aveTurnaroundTime = 0

# Load data from file
with open ('output/ContextSwitch/FCFS_4_5.txt') as file:
    lines = file.readlines()
    
    data = []

    for line in lines:
        data.append(line.split(", "))
    
# Find the start time for each process
for i in range(1,18+1):
    pid = str(i).zfill(3)

    for action in data:
        if action[0] == pid:
            startTimes.append(action[3])
            break

# Find the end time for each process
for i in range(18,0,-1):
    pid = str(i).zfill(3)

    for action in data[::-1]:
        if action[0] == pid:
            endTimes.insert(0, action[3])
            break

# Find average turnaround time among the processes
totalTime = 0

for i in range(0, 18):
    turnaroundTimes.append(int(endTimes[i]) - int(startTimes[i]))
    totalTime += turnaroundTimes[i]

aveTurnaroundTime = round(totalTime / 18)

# Write to file
file = open("TurnaroundTimes_ContextSwitch.txt", "a")
file.write("FCFS_4_5 ContextSwitch : " + str(aveTurnaroundTime) + "\n")
file.close()