; Test POP AX
; Pops a value from the stack into AX.
; Expected: PUSH 77 => Stack [77]; POP AX => AX=77
MOV AX, 77
PUSH AX
MOV AX, 0
POP AX
