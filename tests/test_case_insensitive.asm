; Test case insensitive handling
; Tests that lowercase instructions are accepted (parser uppercases them).
; Expected: AX=42, BX=10
mov ax, 42
load ax
mov bx, 10
add bx
