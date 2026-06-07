; Test SUB register
; Subtracts the register value from AC. AC = AC - register
; Expected: MOV AX,20; LOAD AX => AC=20; MOV BX,7; SUB BX => AC=13
MOV AX, 20
LOAD AX
MOV BX, 7
SUB BX
