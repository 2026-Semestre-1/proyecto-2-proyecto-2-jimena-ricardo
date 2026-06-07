; Test JNE loop (count down from 5)
; Uses JNE to create a loop that counts down from 5 to 0.
; Expected: AX=0 after loop
MOV AX, 5
DEC AX
CMP AX, 0
JNE -2
