; Test INC from negative to zero
; Increments AC from -1 to 0.
; Expected: MOV AX,-1; LOAD AX => AC=-1; INC => AC=0
MOV AX, -1
LOAD AX
INC
