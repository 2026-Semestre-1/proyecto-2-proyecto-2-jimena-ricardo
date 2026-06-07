; Test SUB with negative result
; Subtracts a larger value from AC to get negative result.
; Expected: MOV AX,10; LOAD AX => AC=10; MOV CX,50; SUB CX => AC=-40
MOV AX, 10
LOAD AX
MOV CX, 50
SUB CX
