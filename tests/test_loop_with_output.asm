; Test loop with output
; Counts from 1 to 3 and prints each value.
; Expected: Prints 1, 2, 3 to screen
MOV AX, 0
INC AX
MOV DX, AX
INT 10H
CMP AX, 3
JNE -4
