; Test INC/DEC on all registers with verification
; INC and DEC on each register, then MOV to verify.
; Expected: AX=6, BX=11, CX=21, DX=31 then decrement: AX=5, BX=10, CX=20, DX=30
MOV AX, 5
MOV BX, 10
MOV CX, 20
MOV DX, 30
INC AX
INC BX
INC CX
INC DX
DEC AX
DEC BX
DEC CX
DEC DX
