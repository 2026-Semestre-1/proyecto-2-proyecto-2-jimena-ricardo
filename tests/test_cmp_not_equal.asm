; Test CMP unequal registers
; Compares two registers with different values.
; Expected: AX=10, BX=20; CMP AX, BX sets equalFlag=false
MOV AX, 10
MOV BX, 20
CMP AX, BX
