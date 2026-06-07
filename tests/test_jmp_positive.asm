; Test JMP positive offset
; Jumps forward by skipping instructions.
; Expected: JMP +2 skips MOV AX,99 and MOV BX,88; jumps to MOV CX,77
JMP 2
MOV AX, 99
MOV BX, 88
MOV CX, 77
