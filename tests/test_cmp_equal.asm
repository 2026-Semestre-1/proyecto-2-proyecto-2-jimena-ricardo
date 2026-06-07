; Test CMP equal registers
; Compares two registers with equal values.
; Expected: AX=10, BX=10; CMP AX, BX sets equalFlag=true
MOV AX, 10
MOV BX, 10
CMP AX, BX
