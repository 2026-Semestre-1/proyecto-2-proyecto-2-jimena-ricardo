; Test STORE AX
; Stores the AC value into AX.
; Expected: MOV DX,55; LOAD DX => AC=55; STORE AX => AX=55
MOV DX, 55
LOAD DX
STORE AX
MOV AX, 0
