; Test complete swap-based sort of 2 elements
; Sorts two values using SWAP, CMP, and conditional jumps.
; Expected: AX=10 (smaller), BX=20 (larger) regardless of initial order
MOV AX, 20
MOV BX, 10
CMP AX, BX
JNE 1
JMP 2
JMP 1
SWAP AX, BX
