; Test ADD overflow detection
; Adds two large positive numbers to trigger overflow.
; Expected: AC=100; ADD BX(100) => overflow flag set, AC wraps
MOV AX, 100
LOAD AX
MOV BX, 100
ADD BX
