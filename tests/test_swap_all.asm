; Test SWAP all register pairs
; Swaps values across all register combinations.
; Expected: After swaps, verify each pair was correctly swapped.
MOV AX, 1
MOV BX, 2
MOV CX, 3
MOV DX, 4
SWAP AX, BX
SWAP CX, DX
SWAP AX, CX
