; Test all arithmetic on AC
; Tests ADD, SUB, INC, DEC on AC in sequence.
; Expected: AC starts at 10, +5=15, -3=12, +1=13, -1=12
MOV AX, 10
LOAD AX
MOV BX, 5
ADD BX
MOV CX, 3
SUB CX
INC
DEC
