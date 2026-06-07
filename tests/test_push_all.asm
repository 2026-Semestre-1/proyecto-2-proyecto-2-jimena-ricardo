; Test PUSH all registers
; Pushes all register values onto the stack.
; Expected: Stack contains [10, 20, 30, 40]
MOV AX, 10
MOV BX, 20
MOV CX, 30
MOV DX, 40
PUSH AX
PUSH BX
PUSH CX
PUSH DX
