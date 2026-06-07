; Test all register MOV combinations (reg to reg)
; Tests all 12 possible register-to-register MOV combinations.
; Expected: All registers end up with value 99
MOV AX, 99
MOV BX, AX
MOV CX, BX
MOV DX, CX
MOV AX, DX
MOV BX, CX
