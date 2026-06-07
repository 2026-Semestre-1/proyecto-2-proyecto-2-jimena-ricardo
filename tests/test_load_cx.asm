; Test LOAD CX
; Loads the value stored in CX into the AC.
; Expected: CX=99, then LOAD CX => AC=99
MOV CX, 99
LOAD CX
INT 10H
