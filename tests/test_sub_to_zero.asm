; Test SUB to zero
; Subtracting the same value results in zero.
; Expected: AC=30, SUB BX(30) => AC=0
MOV AX, 30
LOAD AX
MOV BX, 30
SUB BX
