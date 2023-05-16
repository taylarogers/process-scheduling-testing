import os

def assessFile(filename):
    startTimes = []
    endTimes = []
    turnaroundTimes = []
    aveTurnaroundTime = 0

    # Load data from file
    with open (filename) as file:
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

    # Get dataset name
    indexLastSlash = filename.rfind("/")
    indexFirstSlash = filename.find("/")
    indexDot = filename.rfind(".")
    directory = filename[int(indexFirstSlash+1):int(indexLastSlash)]
    datasetName = filename[int(indexLastSlash+1):int(indexDot)]

    # Write to file
    outputFilename = "TurnaroundTimes_" + directory + ".txt"
    file = open(outputFilename, "a")
    file.write(datasetName + " " + directory + " : " + str(aveTurnaroundTime) + "\n")
    file.close()

def getFiles(directory):
    path = "output/" + directory + "/"

    for file in os.listdir(path):
        assessFile("output/" + directory + "/" + file)

getFiles("ContextSwitch")
getFiles("TimeQuantum")