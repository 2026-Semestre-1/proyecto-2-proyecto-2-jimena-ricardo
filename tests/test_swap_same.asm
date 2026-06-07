; Test SWAP with same values
; Swaps registers that hold the same value (no visible change).
; Expected: AX=10, BX=10; SWAP AX, BX => AX=10, BX=10
MOV AX, 10
MOV BX, 10
SWAP AX, BX
