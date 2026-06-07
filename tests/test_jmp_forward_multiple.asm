; Test JMP forward skipping multiple instructions
; Jumps forward by 3, skipping 3 instructions.
; Expected: AX=1, JMP +3 skips MOV BX,CX,DX; CX=4
MOV AX, 1
JMP 3
MOV BX, 2
MOV CX, 3
MOV DX, 4
MOV CX, 4
