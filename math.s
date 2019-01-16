.pos 0x100
                 ld   $a, r0              # r0 = address of a
                 ld   $b, r1              # r1 = address of b
                 ld   (r1), r2            # r2 = b
                 mov  r2, r3              # r3 = r2
                 inc  r3                  # r3 = r3 + 1
                 inca r3                  # r3 = r3 + 4
                 shr  $0x1, r3            # r3 = r3 << -1
                 and  r2, r3              # r3 = r3 & r2
                 shl  $0x2, r3            # r3 = r3 << 2
                 st   r3, (r0)            # a = r3
                 halt                     # halt
.pos 0x1000
a:               .long 0xffffffff         # a
.pos 0x1004
b:               .long 0xffffffff         # b