; Test INC (without register)
; Increments AC by 1.
; Expected: MOV AX,10; LOAD AX => AC=10; INC => AC=11
MOV AX, 10
LOAD AX
INC
