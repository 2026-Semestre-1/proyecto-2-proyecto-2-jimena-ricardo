; Test PUSH/POP with multiple registers
; Pushes and pops across all registers.
; Expected: After operations, BX=10, CX=20, DX=30
MOV AX, 10
PUSH AX
MOV AX, 20
PUSH AX
MOV AX, 30
PUSH AX
POP DX
POP CX
POP BX
