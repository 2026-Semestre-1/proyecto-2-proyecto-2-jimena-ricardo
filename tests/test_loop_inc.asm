; Test loop with INC and CMP/JNE
; Creates a loop that increments AX from 0 to 5.
; Expected: AX=5 after loop completion
MOV AX, 0
INC AX
CMP AX, 5
JNE -2
