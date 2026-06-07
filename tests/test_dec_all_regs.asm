; Test DEC BX, CX, DX
; Decrements each register by 1.
; Expected: BX=9, CX=19, DX=29
MOV BX, 10
MOV CX, 20
MOV DX, 30
DEC BX
DEC CX
DEC DX
