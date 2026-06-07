; Test ADD and SUB combined with LOAD/STORE
; Computes (10 + 5 - 3) and stores result in DX.
; Expected: AC=12, STORE DX => DX=12
MOV AX, 10
LOAD AX
MOV BX, 5
ADD BX
MOV CX, 3
SUB CX
STORE DX
