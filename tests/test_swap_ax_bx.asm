; Test SWAP AX, BX
; Swaps values between AX and BX.
; Expected: AX=10, BX=20; SWAP AX, BX => AX=20, BX=10
MOV AX, 10
MOV BX, 20
SWAP AX, BX
