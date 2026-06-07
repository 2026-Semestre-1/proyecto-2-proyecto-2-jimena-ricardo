; Test ADD chaining multiple adds
; Chains multiple ADD instructions.
; Expected: AC=10; ADD BX(5)=15; ADD CX(3)=18; ADD DX(2)=20
MOV AX, 10
LOAD AX
MOV BX, 5
MOV CX, 3
MOV DX, 2
ADD BX
ADD CX
ADD DX
