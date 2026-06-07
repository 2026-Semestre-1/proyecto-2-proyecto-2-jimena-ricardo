; Test LOAD DX
; Loads the value stored in DX into the AC.
; Expected: DX=200, then LOAD DX => AC=200
MOV DX, 200
LOAD DX
INT 10H
