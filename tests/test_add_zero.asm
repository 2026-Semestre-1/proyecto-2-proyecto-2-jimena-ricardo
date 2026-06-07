; Test ADD with zero
; Adding zero should not change AC.
; Expected: AC=50, ADD BX(0) => AC=50
MOV AX, 50
LOAD AX
MOV BX, 0
ADD BX
