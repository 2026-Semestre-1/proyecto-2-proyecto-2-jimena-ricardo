; Test INT 10H with negative value
; Prints a negative value from DX.
; Expected: DX=-25, INT 10H prints "-25"
MOV DX, -25
INT 10H
