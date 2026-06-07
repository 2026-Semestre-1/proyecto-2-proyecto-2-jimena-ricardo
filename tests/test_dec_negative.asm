; Test DEC from positive to negative
; Decrements AC from 0 to -1.
; Expected: MOV AX,0; LOAD AX => AC=0; DEC => AC=-1
MOV AX, 0
LOAD AX
DEC
