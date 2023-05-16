import os

output_directory = "output"

# Function to calculate average waiting time
# Function to calculate average waiting time
def calculate_average_waiting_time(file_path):
    with open(file_path, 'r') as file:
        content = file.readlines()

    waiting_time = 0
    count = 0

    for line in content:
        line = line.strip().split(', ')
        state = line[1]

        # Check if the start and end times can be converted to integers
        try:
            start_time = int(line[3])
            end_time = int(line[4])
        except ValueError:
            continue  # Skip the line if start or end time is not a valid integer

        if state == 'READY':
            waiting_time += (end_time - start_time)
            count += 1

    average_waiting_time = waiting_time / count if count > 0 else 0
    return average_waiting_time

# Function to process files in a directory and save results to a text file
def process_directory(directory):
    directory_path = os.path.join(output_directory, directory)
    output_file_name = f"{directory}_waitingTimes.txt"
    output_file_path = os.path.join(directory_path, output_file_name)

    with open(output_file_path, 'w') as output_file:
        for file_name in os.listdir(directory_path):
            file_path = os.path.join(directory_path, file_name)
            average_waiting_time = calculate_average_waiting_time(file_path)
            output_line = f"{file_name} {directory} : {average_waiting_time:.2f}\n"
            output_file.write(output_line)

    print(f"Average waiting times for files in '{directory}' have been saved to '{output_file_path}'.")


# Process files in the ContextSwitch directory
process_directory("ContextSwitch")

# Process files in the TimeQuantum directory
process_directory("TimeQuantum")
