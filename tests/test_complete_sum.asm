; Test complete program: compute factorial-like sum (1+2+3+4+5)
; Accumulates values 1 through 5 using a loop.
; Expected: DX=15 (sum of 1..5)
MOV AX, 1
MOV BX, 5
MOV CX, 0
MOV DX, 0
LOAD CX
ADD AX
STORE CX
INC AX
CMP AX, BX
JNE -5
STORE DX
INT 10H
