; Test LOAD BX
; Loads the value stored in BX into the AC.
; Expected: BX=17, then LOAD BX => AC=17
MOV BX, 17
LOAD BX
INT 10H
