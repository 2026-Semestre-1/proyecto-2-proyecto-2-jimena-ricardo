; Test PUSH AX
; Pushes the value of AX onto the stack.
; Expected: AX=42, PUSH AX => Stack contains [42]
MOV AX, 42
PUSH AX
