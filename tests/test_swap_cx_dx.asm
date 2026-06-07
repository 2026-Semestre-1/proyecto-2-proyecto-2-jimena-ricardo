; Test SWAP CX, DX
; Swaps values between CX and DX.
; Expected: CX=55, DX=99; SWAP CX, DX => CX=99, DX=55
MOV CX, 55
MOV DX, 99
SWAP CX, DX
