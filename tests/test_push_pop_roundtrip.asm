; Test PUSH and POP round-trip
; Pushes a value and pops it into a different register.
; Expected: AX=55, PUSH AX => Stack [55]; POP BX => BX=55
MOV AX, 55
PUSH AX
POP BX
