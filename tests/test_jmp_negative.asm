; Test JMP negative offset (loop)
; Jumps backward creating a simple loop.
; Expected: AX starts at 0, INC AX three times via loop, then exits when AX=3
MOV AX, 0
INC AX
CMP AX, 3
JNE -2
