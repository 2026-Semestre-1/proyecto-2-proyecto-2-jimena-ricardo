; Test STORE CX
; Stores the AC value into CX.
; Expected: MOV AX,88; LOAD AX => AC=88; STORE CX => CX=88
MOV AX, 88
LOAD AX
STORE CX
MOV CX, 0
