; Test STORE DX
; Stores the AC value into DX.
; Expected: MOV BX,120; LOAD BX => AC=120; STORE DX => DX=120
MOV BX, 120
LOAD BX
STORE DX
MOV DX, 0
