; Test DEC (without register)
; Decrements AC by 1.
; Expected: MOV AX,10; LOAD AX => AC=10; DEC => AC=9
MOV AX, 10
LOAD AX
DEC
