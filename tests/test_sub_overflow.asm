; Test SUB overflow detection
; Subtracts a large positive from a large negative to trigger overflow.
; Expected: AC=-100; SUB BX(100) => overflow flag set, AC wraps
MOV AX, -100
LOAD AX
MOV BX, 100
SUB BX
