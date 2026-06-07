; Test PARAM then POP (parameter passing)
; Uses PARAM to push values, then POP to retrieve them.
; Expected: AX=100, BX=200, CX=300
PARAM 100, 200, 300
POP AX
POP BX
POP CX
