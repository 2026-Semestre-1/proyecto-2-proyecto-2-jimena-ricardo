; Test complete program with INT 10H output
; Builds a value and prints it using INT 10H.
; Expected: DX=42, prints "42" to screen
MOV AX, 20
LOAD AX
MOV BX, 22
ADD BX
STORE DX
INT 10H
