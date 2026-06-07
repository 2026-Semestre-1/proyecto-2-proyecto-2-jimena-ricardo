; Test MOV register, register
; Copies the value from one register to another.
; Expected: AX=77, then MOV BX, AX => BX=77; MOV CX, BX => CX=77
MOV AX, 77
MOV BX, AX
MOV CX, BX
