; Test comprehensive arithmetic chain
; Tests a full arithmetic computation: result = (AX * 2) + BX - CX
; Simulated as: result = AX + AX + BX - CX
; Expected: 10+10+5-3 = 22
MOV AX, 10
LOAD AX
ADD AX
MOV BX, 5
ADD BX
MOV CX, 3
SUB CX
