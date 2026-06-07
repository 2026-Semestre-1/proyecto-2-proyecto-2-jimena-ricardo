; Test INC BX, CX, DX
; Increments each register by 1.
; Expected: BX=11, CX=21, DX=31
MOV BX, 10
MOV CX, 20
MOV DX, 30
INC BX
INC CX
INC DX
