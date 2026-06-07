; Test LOAD AX
; Loads the value stored in AX into the AC (accumulator).
; Expected: AX=42, then LOAD AX => AC=42
MOV AX, 42
LOAD AX
INT 10H
