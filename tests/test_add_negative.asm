; Test ADD with negative result
; Adds negative value to AC.
; Expected: MOV AX,50; LOAD AX => AC=50; MOV CX,-80; ADD CX => AC=-30
MOV AX, 50
LOAD AX
MOV CX, -80
ADD CX
