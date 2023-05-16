#!/bin/bash

# Empty the output file if it exists
> output.txt

for input_file in data/ContextSwitch/*.inp
do
    output=$(java -ea -cp bin Simulate $input_file)
    echo $output >> ContextSwithOutput.txt
done

for input_file in data/TimeQuantum/*.inp
do
    output=$(java -ea -cp bin Simulate $input_file)
    echo $output >> TimeQuatumOutput.txt
done