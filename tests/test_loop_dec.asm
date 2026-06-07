; Test loop with DEC and CMP/JNE
; Creates a loop that decrements AX from 10 to 5.
; Expected: AX=5 after loop completion
MOV AX, 10
DEC AX
CMP AX, 5
JNE -2
