; Test CMP with all register pairs
; Compares different register combinations.
; Expected: equalFlag set for equal pairs, not set for unequal pairs
MOV AX, 10
MOV BX, 10
MOV CX, 20
MOV DX, 20
CMP AX, BX
CMP CX, DX
CMP AX, CX
