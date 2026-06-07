; Test INT 10H
; Prints the value of DX to screen.
; Expected: DX=42, INT 10H prints "42"
MOV DX, 42
INT 10H
