package arch.sm213.machine.student;

import arch.sm213.machine.AbstractSM213CPU;
import machine.AbstractMainMemory;
import machine.RegisterSet;
import util.UnsignedByte;


/**
 * The Simple Machine CPU.
 * <p>
 * Simulate the execution of a single cycle of the Simple Machine SM213 CPU.
 */

public class CPU extends AbstractSM213CPU {

    /**
     * Create a new CPU.
     *
     * @param name   fully-qualified name of CPU implementation.
     * @param memory main memory used by CPU.
     */
    public CPU(String name, AbstractMainMemory memory) {
        super(name, memory);
    }

    /**
     * Fetch Stage of CPU Cycle.
     * Fetch instruction at address stored in "pc" register from memory into instruction register
     * and set "pc" to point to the next instruction to execute.
     * <p>
     * Input register:   pc.
     * Output registers: pc, instruction, insOpCode, insOp0, insOp1, insOp2, insOpImm, insOpExt
     *
     * @throws MainMemory.InvalidAddressException when program counter contains an invalid memory address
     * @see AbstractSM213CPU for pc, instruction, insOpCode, insOp0, insOp1, insOp2, insOpImm, insOpExt
     */
    @Override
    protected void fetch() throws MainMemory.InvalidAddressException {
        int pcVal = pc.get();
        UnsignedByte[] ins = mem.read(pcVal, 2);
        byte opCode = (byte) (ins[0].value() >>> 4);
        insOpCode.set(opCode);
        insOp0.set(ins[0].value() & 0x0f);
        insOp1.set(ins[1].value() >>> 4);
        insOp2.set(ins[1].value() & 0x0f);
        insOpImm.set(ins[1].value());
        pcVal += 2;
        switch (opCode) {
            case 0x0:
            case 0xb:
                long opExt = mem.readIntegerUnaligned(pcVal) & 0xffffffffL;
                pcVal += 4;
                insOpExt.set(opExt);
                instruction.set(ins[0].value() << 40 | ins[1].value() << 32 | opExt);
                break;
            default:
                insOpExt.set(0);
                instruction.set(ins[0].value() << 40 | ins[1].value() << 32);
        }
        pc.set(pcVal);
    }


    /**
     * Execution Stage of CPU Cycle.
     * Execute instruction that was fetched by Fetch stage.
     * <p>
     * Input state: pc, instruction, insOpCode, insOp0, insOp1, insOp2, insOpImm, insOpExt, reg, mem
     * Ouput state: pc, reg, mem
     *
     * @throws InvalidInstructionException                when instruction format is invalid.
     * @throws MachineHaltException                       when instruction is the HALT instruction.
     * @throws RegisterSet.InvalidRegisterNumberException when instruction references an invalid register (i.e, not 0-7).
     * @throws MainMemory.InvalidAddressException         when instruction references an invalid memory address.
     * @see AbstractSM213CPU for pc, instruction, insOpCode, insOp0, insOp1, insOp2, insOpImm, insOpExt
     * @see MainMemory       for mem
     * @see machine.AbstractCPU      for reg
     */
    @Override
    protected void execute() throws InvalidInstructionException, MachineHaltException, RegisterSet.InvalidRegisterNumberException, MainMemory.InvalidAddressException {
        switch (insOpCode.get()) {
            case 0x0: // ld $v, d .............. 0d-- vvvv vvvv
                reg.set(insOp0.get(), insOpExt.get());
                break;
            case 0x1: // ld o(rs), rd .......... 1psd  (p = o / 4)
                reg.set(insOp2.get(), mem.readInteger((insOp0.get() << 2) + reg.get(insOp1.get())));
                break;
            case 0x2: // ld (rs, ri, 4), rd .... 2sid
                int source_base_address_register = insOp0.get();
                int source_base_address = reg.get(source_base_address_register);
                int source_index_register = insOp1.get();
                int source_index = reg.get(source_index_register);
                int source_address_offset = source_index << 2;
                int address = source_base_address + source_address_offset;
                reg.set(insOp2.get(), mem.readInteger(address));
                break;
            case 0x3: // st rs, o(rd) .......... 3spd  (p = o / 4)
                int destination_address_offset = insOp1.get() << 2;
                int destination_base_address_register = insOp2.get();
                int destination_base_address = reg.get(destination_base_address_register);
                int destination_address = destination_base_address + destination_address_offset;
                int source_register = insOp0.get();
                int source_value = reg.get(source_register);
                mem.writeInteger(destination_address, source_value);
                break;
            case 0x4: // st rs, (rd, ri, 4) .... 4sdi
                source_register = insOp0.get();
                destination_base_address_register = insOp1.get();
                int destination_index_register= insOp2.get();
                destination_base_address = reg.get(destination_base_address_register);
                int destination_index = reg.get(destination_index_register);
                destination_address_offset = destination_index << 2;
                destination_address = destination_base_address + destination_address_offset;
                source_value = reg.get(source_register);
                mem.writeInteger(destination_address, source_value);
                break;
            case 0x6: // ALU ................... 6-sd
                switch (insOp0.get()) {
                    case 0x0: // mov rs, rd ........ 60sd
                        source_register = insOp1.get();
                        int destination_register = insOp2.get();
                        source_value = reg.get(source_register);
                        reg.set(destination_register, source_value);
                        break;
                    case 0x1: // add rs, rd ........ 61sd
                        source_register = insOp1.get();
                        destination_register = insOp2.get();
                        source_value = reg.get(source_register);
                        int destination_value = reg.get(destination_register);
                        int result = source_value + destination_value;
                        reg.set(destination_register, result);
                        break;
                    case 0x2: // and rs, rd ........ 62sd
                        source_register = insOp1.get();
                        destination_register = insOp2.get();
                        source_value = reg.get(source_register);
                        destination_value = reg.get(destination_register);
                        result = source_value & destination_value;
                        reg.set(destination_register, result);
                        break;
                    case 0x3: // inc rr ............ 63-r
                        int register = insOp2.get();
                        int value = reg.get(register);
                        reg.set(register, value + 1);
                        break;
                    case 0x4: // inca rr ........... 64-r
                        register = insOp2.get();
                        value = reg.get(register);
                        reg.set(register, value + 4);
                        break;
                    case 0x5: // dec rr ............ 65-r
                        register = insOp2.get();
                        value = reg.get(register);
                        reg.set(register, value - 1);
                        break;
                    case 0x6: // deca rr ........... 66-r
                        register = insOp2.get();
                        value = reg.get(register);
                        reg.set(register, value - 4);
                        break;
                    case 0x7: // not ............... 67-r
                        register = insOp2.get();
                        value = reg.get(register);
                        reg.set(register, ~value);
                        break;
                    default:
                        throw new InvalidInstructionException();
                }
                break;
            case 0x7: // sh[rl] $i,rd ............. 7dii
                if (insOpImm.get() > 0) {
                    reg.set(insOp0.get(), reg.get(insOp0.get()) << insOpImm.get());
                }
                else
                    // shr $i,rd ............. 7dii
                    reg.set(insOp0.get(), reg.get(insOp0.get()) >> -insOpImm.get());
                break;
            case 0xf: // halt or nop ............. f?--
                if (insOp0.get() == 0)
                    // halt .......................... f0--
                    throw new MachineHaltException();
                else if (insOp0.getUnsigned() == 0xf)
                    // nop ........................... ff--
                    break;
                break;
            default:
                throw new InvalidInstructionException();
        }
    }
}
