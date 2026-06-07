; Test JE loop (count to 3)
; Uses JE to create a loop that counts to 3.
; Expected: AX=3 after loop
MOV AX, 0
INC AX
CMP AX, 3
JE 1
JMP -3
