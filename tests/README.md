## LOAD

| File | Description |
|---|---|
| `test_load_ax.asm` | Loads value from AX into AC (AX=42 → AC=42) |
| `test_load_bx.asm` | Loads value from BX into AC (BX=17 → AC=17) |
| `test_load_cx.asm` | Loads value from CX into AC (CX=99 → AC=99) |
| `test_load_dx.asm` | Loads value from DX into AC (DX=200 → AC=200) |

## STORE

| File | Description |
|---|---|
| `test_store_ax.asm` | Stores AC value into AX (AC=55 → AX=55) |
| `test_store_bx.asm` | Stores AC value into BX (AC=30 → BX=30) |
| `test_store_cx.asm` | Stores AC value into CX (AC=88 → CX=88) |
| `test_store_dx.asm` | Stores AC value into DX (AC=120 → DX=120) |

## MOV

| File | Description |
|---|---|
| `test_mov_reg_value.asm` | MOV immediate values to all 4 registers |
| `test_mov_reg_reg.asm` | MOV register-to-register (AX→BX→CX chain) |
| `test_mov_negative.asm` | MOV with negative value (-50) |
| `test_mov_zero.asm` | MOV zero to all registers |
| `test_mov_max_value.asm` | MOV max positive value (127) |
| `test_mov_min_value.asm` | MOV min negative value (-128) |
| `test_mov_all_reg_combos.asm` | MOV across all register combinations |

## ADD

| File | Description |
|---|---|
| `test_add_basic.asm` | Basic ADD (AC=10+5=15) |
| `test_add_negative.asm` | ADD with negative result (AC=50+(-80)=-30) |
| `test_add_multiple.asm` | Chained ADD (10+5+3+2=20) |
| `test_add_zero.asm` | ADD zero (no change) |
| `test_add_overflow.asm` | ADD overflow detection (100+100) |

## SUB

| File | Description |
|---|---|
| `test_sub_basic.asm` | Basic SUB (AC=20-7=13) |
| `test_sub_negative.asm` | SUB with negative result (AC=10-50=-40) |
| `test_sub_zero.asm` | SUB from zero (AC=0-5=-5) |
| `test_sub_to_zero.asm` | SUB to exactly zero (AC=30-30=0) |
| `test_sub_overflow.asm` | SUB overflow detection (-100-100) |

## INC

| File | Description |
|---|---|
| `test_inc_ac.asm` | INC without register (increments AC: 10→11) |
| `test_inc_ax.asm` | INC AX (49→50) |
| `test_inc_all_regs.asm` | INC on BX, CX, DX |
| `test_inc_negative.asm` | INC from -1 to 0 |

## DEC

| File | Description |
|---|---|
| `test_dec_ac.asm` | DEC without register (decrements AC: 10→9) |
| `test_dec_ax.asm` | DEC AX (51→50) |
| `test_dec_all_regs.asm` | DEC on BX, CX, DX |
| `test_dec_negative.asm` | DEC from 0 to -1 |

## SWAP

| File | Description |
|---|---|
| `test_swap_ax_bx.asm` | SWAP AX,BX (10,20 → 20,10) |
| `test_swap_cx_dx.asm` | SWAP CX,DX (55,99 → 99,55) |
| `test_swap_all.asm` | Multiple SWAP operations across registers |
| `test_swap_same.asm` | SWAP with equal values (no visible change) |
| `test_swap_double.asm` | Double SWAP restores original values |
| `test_swap_all_pairs.asm` | All possible SWAP register pair combinations |
| `test_swap_sort.asm` | Sort two values using SWAP with conditional jumps |

## INT 10H

| File | Description |
|---|---|
| `test_int10h_basic.asm` | Prints DX=42 to screen |
| `test_int10h_zero.asm` | Prints DX=0 to screen |
| `test_int10h_multiple.asm` | Prints multiple values (10, 20, 30) |
| `test_int10h_negative.asm` | Prints DX=-25 to screen |

## JMP

| File | Description |
|---|---|
| `test_jmp_positive.asm` | JMP forward (+2 skips instructions) |
| `test_jmp_negative.asm` | JMP backward (creates a loop) |
| `test_jmp_forward_multiple.asm` | JMP forward +3 skipping 3 instructions |

## CMP

| File | Description |
|---|---|
| `test_cmp_equal.asm` | CMP with equal registers (AX=10, BX=10) |
| `test_cmp_not_equal.asm` | CMP with unequal registers (AX=10, BX=20) |
| `test_cmp_all_pairs.asm` | CMP across different register pairs |
| `test_cmp_je_jne_sequence.asm` | CMP followed by both JE and JNE |

## JE

| File | Description |
|---|---|
| `test_je_equal.asm` | JE jumps when equal (skips instruction) |
| `test_je_not_equal.asm` | JE does NOT jump when not equal |
| `test_je_loop.asm` | JE used in a counting loop (count to 3) |

## JNE

| File | Description |
|---|---|
| `test_jne_not_equal.asm` | JNE jumps when not equal (skips instruction) |
| `test_jne_equal.asm` | JNE does NOT jump when equal |
| `test_jne_countdown.asm` | JNE used in countdown loop (5→0) |

## PARAM

| File | Description |
|---|---|
| `test_param_single.asm` | PARAM with 1 value |
| `test_param_two.asm` | PARAM with 2 values |
| `test_param_three.asm` | PARAM with 3 values (max allowed) |
| `test_param_negative.asm` | PARAM with negative values |
| `test_param_mixed.asm` | PARAM with mixed positive/negative values |
| `test_param_pop.asm` | PARAM then POP to retrieve values into registers |

## PUSH/POP

| File | Description |
|---|---|
| `test_push_ax.asm` | PUSH AX (value 42 onto stack) |
| `test_push_all.asm` | PUSH all 4 registers |
| `test_pop_ax.asm` | POP value from stack into AX |
| `test_push_pop_roundtrip.asm` | PUSH AX then POP BX (value transfer) |
| `test_push_pop_lifo.asm` | PUSH/POP LIFO ordering verification |
| `test_push_pop_restore.asm` | PUSH to save, modify, POP to restore |

## Combination / Integration (11 files)

| File | Description |
|---|---|
| `test_inc_dec_all.asm` | INC then DEC on all registers |
| `test_inc_dec_cancel.asm` | INC+DEC cancel each other on AC |
| `test_all_arithmetic_ac.asm` | All arithmetic ops on AC (ADD, SUB, INC, DEC) |
| `test_add_sub_combined.asm` | Compute (10+5-3)=12 and store to DX |
| `test_arithmetic_chain.asm` | Full computation: 10+10+5-3=22 |
| `test_load_store_chain.asm` | LOAD/STORE chain across all registers |
| `test_complete_sum.asm` | Loop summing 1..5=15 with INT 10H output |
| `test_loop_with_output.asm` | Loop counting 1→3 with INT 10H prints each step |
| `test_compute_and_print.asm` | Compute 20+22=42 and print via INT 10H |
| `test_empty_lines.asm` | Empty lines between instructions are properly skipped |
| `test_case_insensitive.asm` | Lowercase instructions accepted (parser uppercases) |
