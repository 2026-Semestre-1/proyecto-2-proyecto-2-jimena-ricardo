; Test JNE when equal (should not jump)
; JNE does NOT jump if equalFlag is set.
; Expected: CMP equal => JNE does not jump; AX becomes 99
MOV AX, 10
MOV BX, 10
CMP AX, BX
JNE 1
MOV AX, 99
