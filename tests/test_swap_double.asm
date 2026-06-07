; Test SWAP preserves values
; Swaps registers twice to verify values are preserved.
; Expected: After double swap, AX=10, BX=20 (back to original)
MOV AX, 10
MOV BX, 20
SWAP AX, BX
SWAP AX, BX
