; Test CMP then JE and JNE in sequence
; Tests both JE and JNE after a single CMP.
; Expected: Since AX=BX, JE jumps (AX becomes 100), JNE does not jump (BX becomes 200)
MOV AX, 10
MOV BX, 10
CMP AX, BX
JE 1
MOV AX, 50
MOV AX, 100
CMP AX, BX
JNE 1
MOV BX, 99
MOV BX, 200
