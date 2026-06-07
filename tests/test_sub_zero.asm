; Test SUB from zero
; Subtracts from zero AC.
; Expected: MOV AX,0; LOAD AX => AC=0; MOV BX,5; SUB BX => AC=-5
MOV AX, 0
LOAD AX
MOV BX, 5
SUB BX
