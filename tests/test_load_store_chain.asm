; Test MOV AX then LOAD/STORE then verify with MOV reg,reg
; Full register transfer chain.
; Expected: AX=42, BX=42, CX=42, DX=42
MOV AX, 42
LOAD AX
STORE BX
STORE CX
STORE DX
