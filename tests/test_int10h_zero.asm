; Test INT 10H with zero
; Prints the value 0 from DX to screen.
; Expected: DX=0, INT 10H prints "0"
MOV DX, 0
INT 10H
