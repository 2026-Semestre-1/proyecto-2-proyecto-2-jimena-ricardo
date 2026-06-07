; Test empty line handling
; Tests that empty lines are properly skipped.
; Expected: AX=10, BX=20, CX=30 (empty lines between instructions are ignored)
MOV AX, 10

MOV BX, 20

MOV CX, 30

