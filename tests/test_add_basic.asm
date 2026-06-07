; Test ADD register
; Adds the register value to AC. AC = AC + register
; Expected: MOV AX,10; LOAD AX => AC=10; MOV BX,5; ADD BX => AC=15
MOV AX, 10
LOAD AX
MOV BX, 5
ADD BX
