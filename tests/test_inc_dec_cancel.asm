; Test INC and DEC cancel each other
; INC then DEC returns to original value.
; Expected: AC=50, INC => 51, DEC => 50
MOV AX, 50
LOAD AX
INC
DEC
