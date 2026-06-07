; Test INT 10H multiple prints
; Prints several values to screen sequentially.
; Expected: Prints 10, 20, 30 on separate outputs
MOV DX, 10
INT 10H
MOV DX, 20
INT 10H
MOV DX, 30
INT 10H
