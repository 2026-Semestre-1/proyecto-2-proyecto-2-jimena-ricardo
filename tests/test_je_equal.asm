; Test JE when equal (should jump)
; JE jumps if equalFlag is set (CMP found equal values).
; Expected: CMP equal => JE jumps over MOV AX,99; AX remains 10
MOV AX, 10
MOV BX, 10
CMP AX, BX
JE 1
MOV AX, 99
