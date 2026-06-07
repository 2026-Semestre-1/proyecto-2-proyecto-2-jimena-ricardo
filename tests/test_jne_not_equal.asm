; Test JNE when not equal (should jump)
; JNE jumps if equalFlag is NOT set (CMP found different values).
; Expected: CMP not equal => JNE jumps over MOV AX,99; AX remains 10
MOV AX, 10
MOV BX, 20
CMP AX, BX
JNE 1
MOV AX, 99
