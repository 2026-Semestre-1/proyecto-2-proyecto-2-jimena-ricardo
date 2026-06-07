; Test JE when not equal (should not jump)
; JE does NOT jump if equalFlag is not set.
; Expected: CMP not equal => JE does not jump; AX becomes 99
MOV AX, 10
MOV BX, 20
CMP AX, BX
JE 1
MOV AX, 99
