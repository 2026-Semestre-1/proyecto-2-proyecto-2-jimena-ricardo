; Test STORE BX
; Stores the AC value into BX.
; Expected: MOV CX,30; LOAD CX => AC=30; STORE BX => BX=30
MOV CX, 30
LOAD CX
STORE BX
MOV BX, 0
