.pos 0x100
                 ld   $a, r0              # r0 = address of a
                 ld   (r0), r1            # r1 = a[0]
                 ld   $0x1, r3            # r3 = 1
                 ld   (r0, r3, 4), r2   # r2 = a[1]
                 st   r2, (r0)            # a[0] = a[1]
                 st   r1, (r0, r3, 4)   # a[1] = r1
                 halt                     # halt
.pos 0x1000
a:               .long 0xffffffff         # a[0]
                 .long 0xffffffff         # a[1]