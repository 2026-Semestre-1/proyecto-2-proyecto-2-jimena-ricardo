; Test PUSH and POP with intermediate operations
; Pushes a value, does other work, then pops to restore.
; Expected: AX=100, push, modify AX=999, pop to restore AX=100
MOV AX, 100
PUSH AX
MOV AX, 999
POP AX
