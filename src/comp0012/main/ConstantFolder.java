package comp0012.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	ConstantPoolGen CPGen = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	private boolean isLoadOp(Instruction instruction) {
        if (instruction instanceof LDC) // loads int, float, String, Class from the constant pool onto the stack 
            return true;
        if (instruction instanceof LDC2_W) //loads long or double type into the stack
            return true;
        if (instruction instanceof ConstantPushInstruction) { //push instruction that produces a literal on the stack -- bytes, 
            return true;
        }
        return false;
    }

    private int foldUnaryOperand(CPInstruction handle, Instruction operand, ConstantPoolGen cpgen) {
        int index = -1;
        if (operand instanceof I2L) {
            int value = (Integer) loadDataValue((LDC) handle, cpgen);
            index = cpgen.addConstant(new ConstantLong((long) value), cpgen);
        } else if (operand instanceof I2F) {
            int value = (Integer) loadDataValue((LDC) handle, cpgen);
            index = cpgen.addConstant(new ConstantFloat((float) value), cpgen);
        } else if (operand instanceof I2D) {
            int value = (Integer) loadDataValue((LDC) handle, cpgen);
            index = cpgen.addConstant(new ConstantDouble((double) value), cpgen);
        } else if (operand instanceof L2I) {
            long value = (Long) loadDataValue((LDC2_W) handle, cpgen);
            index = cpgen.addConstant(new ConstantInteger((int) value), cpgen);
        } else if (operand instanceof L2F) {
            long value = (Long) loadDataValue((LDC2_W) handle, cpgen);
            index = cpgen.addConstant(new ConstantFloat((float) value), cpgen);
        } else if (operand instanceof L2D) {
            long value = (Long) loadDataValue((LDC2_W) handle, cpgen);
            index = cpgen.addConstant(new ConstantDouble((double) value), cpgen);
        } else if (operand instanceof D2F) {
            double value = (Double) loadDataValue((LDC2_W) handle, cpgen);
            index = cpgen.addConstant(new ConstantFloat((float) value), cpgen);
        } else if (operand instanceof D2I) {
            double value = (Double) loadDataValue((LDC2_W) handle, cpgen);
            index = cpgen.addConstant(new ConstantInteger((int) value), cpgen);
        } else if (operand instanceof D2L) {
            double value = (Double) loadDataValue((LDC2_W) handle, cpgen);
            index = cpgen.addConstant(new ConstantLong((long) value), cpgen);
        } else if (operand instanceof F2D) {
            float value = (Float) loadDataValue((LDC) handle, cpgen);
            index = cpgen.addConstant(new ConstantDouble((double) value), cpgen);
        } else if (operand instanceof F2I) {
            float value = (Float) loadDataValue((LDC) handle, cpgen);
            index = cpgen.addConstant(new ConstantInteger((int) value), cpgen);
        } else if (operand instanceof F2L) {
            float value = (Float) loadDataValue((LDC) handle, cpgen);
            index = cpgen.addConstant(new ConstantLong((long) value), cpgen);
        } else if (operand instanceof INEG) {
            int value = (int) loadDataValue((LDC) handle, cpgen);
            index = cpgen.addConstant(new ConstantInteger(0 - value), cpgen);
        } else if (operand instanceof FNEG) {
            float value = (float) loadDataValue((LDC) handle, cpgen);
            index = cpgen.addConstant(new ConstantFloat(0.0f - value), cpgen);
        } else if (operand instanceof DNEG) {
            double value = (double) loadDataValue((LDC2_W) handle, cpgen);
            index = cpgen.addConstant(new ConstantDouble(0.0 - value), cpgen);
        } else if (operand instanceof LNEG) {
            long value = (long) loadDataValue((LDC2_W) handle, cpgen);
            index = cpgen.addConstant(new ConstantLong(0L - value), cpgen);
        }
        return index;
    }


	private int foldUnaryOperand(ConstantPushInstruction handle, Instruction operand, ConstantPoolGen cpgen) {
        int idx = -1;
        if (operand instanceof I2L || operand instanceof F2L || operand instanceof D2L) { // Convert int,float, double to long
            long value = (long) handle.getValue();
			idx = cpgen.addConstant(new ConstantLong(value), cpgen);
		} else if (operand instanceof I2F || operand instanceof D2F || operand instanceof L2F) {
            float value = (float) handle.getValue();
            idx = cpgen.addConstant(new ConstantFloat(value), cpgen);
		}  else if (operand instanceof I2D || operand instanceof F2D || operand instanceof L2D) {
            double value = (double) handle.getValue();
            idx = cpgen.addConstant(new ConstantDouble(value), cpgen);
        } else if (operand instanceof L2I || operand instanceof F2I || operand instanceof D2I) {
            int value = (int) handle.getValue();
            idx = cpgen.addConstant(new ConstantInteger(value), cpgen);
		} else if (operand instanceof INEG) {
            int value = (int) handle.getValue();
            idx = cpgen.addConstant(new ConstantInteger(0 - value), cpgen);
        } else if (operand instanceof FNEG) {
            float value = (float) handle.getValue();
            idx = cpgen.addConstant(new ConstantFloat(0.0f - value), cpgen);
        } else if (operand instanceof DNEG) {
            double value = (double) handle.getValue();
            idx = cpgen.addConstant(new ConstantDouble(0.0 - value), cpgen);
        } else if (operand instanceof LNEG) {
            long value = (long) handle.getValue();
            idx = cpgen.addConstant(new ConstantLong(0L - value), cpgen);
        }
        return idx;
	}

	private void setTarget(int from, int to, ConstantPoolGen cpgen, InstructionList insrtList) {
        InstructionHandle[] handles = insrtList.getInstructionHandles();
        for (InstructionHandle handle : handles) {
            if (handle.getInstruction() instanceof InstructionTargeter) {
                if (((InstructionTargeter) handle.getInstruction()).containsTarget(handles[from])) { //Checks whether this targeter targets the specified instruction handle
                    ((InstructionTargeter) handle.getInstruction()).updateTarget(handles[from], handles[to]); //Replaces the target of this targeter from this old handle to the new handle.
                }
            }
        }
    }

    private void setTarget(InstructionHandle from, InstructionHandle to, ConstantPoolGen cpgen, InstructionList insrtList) {
        InstructionHandle[] handles = insrtList.getInstructionHandles();
        for (InstructionHandle handle : handles) {
            if (handle.getInstruction() instanceof InstructionTargeter) {
                if (((InstructionTargeter) handle.getInstruction()).containsTarget(from)) {
                    ((InstructionTargeter) handle.getInstruction()).updateTarget(from, to);
                }
            }
        }
    }

	private int findTargetIndex(InstructionHandle[] handles, InstructionHandle handle) {
        for (int i = 0; i < handles.length; i++) {
            if (handles[i].equals(handle))
                return i;
        }
        return -1;
    }

	private boolean foldIfElseStatementUnary(InstructionHandle instructionHandle, int value, int index, InstructionHandle[] handles, ConstantPoolGen cpgen, InstructionList instructionList) {
		//        System.out.println(index);
		IfInstruction instruction = (IfInstruction) instructionHandle.getInstruction(); //Super class for the IFxxx family of instructions.
		boolean flag = false;
        if (instruction instanceof IFEQ && value == 0)
            flag = true;
        if (instruction instanceof IFGE && value >= 0)
            flag = true;
        if (instruction instanceof IFLE && value <= 0)
            flag = true;
        if (instruction instanceof IFGT && value > 0)
            flag = true;
        if (instruction instanceof IFLT && value < 0)
            flag = true;
        if (instruction instanceof IFNE && value != 0)
            flag = true;

		InstructionHandle ifTarget = instruction.getTarget();
		
		if (findTargetIndex(handles, ifTarget) < index) {
			return false;
		}
		InstructionHandle gotoTarget = null;
		if (ifTarget.getPrev().getInstruction() instanceof GotoInstruction) {
			gotoTarget = ((GotoInstruction) ifTarget.getPrev().getInstruction()).getTarget();
			if (findTargetIndex(handles, gotoTarget) < findTargetIndex(handles, ifTarget) - 1) {
				return false;
			}
		}
		if (flag) {
			setTarget(handles[index - 1], ifTarget, cpgen, instructionList);
			try {
				instructionList.delete(handles[index - 1], ifTarget.getPrev());
			} catch (TargetLostException e) {
				System.out.println("target lost");
			}
		} else {
			setTarget(handles[index - 1], handles[index + 1], cpgen, instructionList);
			try {
				instructionList.delete(handles[index - 1], handles[index]);
				if (gotoTarget != null) {
					instructionList.delete(ifTarget.getPrev(), gotoTarget.getPrev());
				}
			} catch (TargetLostException e) {
				System.out.println("target lost");
			}
		}
		return true;
	}

    private Number loadDataValue(LDC2_W handle, ConstantPoolGen cpgen) {
        return handle.getValue(cpgen);
    }

    private Object loadDataValue(LDC handle, ConstantPoolGen cpgen) {
        return handle.getValue(cpgen);
    }
	private int calc(PushInstruction handleX, PushInstruction handleY, Instruction operand, ConstantPoolGen cpgen) {
		int index = -1;
		if (operand instanceof IADD) {
            int x;
            int y;
            if (handleX instanceof CPInstruction) {
                x = (int) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (int) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (int) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (int) ((ConstantPushInstruction) handleY).getValue();
            }
            int ans = x + y;
            index = cpgen.addConstant(new ConstantInteger(ans), cpgen);
        } else if (operand instanceof ISUB) {
            int x;
            int y;
            if (handleX instanceof CPInstruction) {
                x = (int) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (int) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (int) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (int) ((ConstantPushInstruction) handleY).getValue();
            }
            int ans = x - y;
            index = cpgen.addConstant(new ConstantInteger(ans), cpgen);
        } else if (operand instanceof IMUL) {
            int x;
            int y;
            if (handleX instanceof CPInstruction) {
                x = (int) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (int) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (int) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (int) ((ConstantPushInstruction) handleY).getValue();
            }
            int ans = x * y;
            index = cpgen.addConstant(new ConstantInteger(ans), cpgen);
        } else if (operand instanceof IDIV) {
            int x;
            int y;
            if (handleX instanceof CPInstruction) {
                x = (int) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (int) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (int) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (int) ((ConstantPushInstruction) handleY).getValue();
            }
            int ans = x / y;
            index = cpgen.addConstant(new ConstantInteger(ans), cpgen);
        } else if (operand instanceof FADD) {
            float x;
            float y;
            if (handleX instanceof CPInstruction) {
                x = (float) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (float) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (float) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (float) ((ConstantPushInstruction) handleY).getValue();
            }
            float ans = x + y;
            index = cpgen.addConstant(new ConstantFloat(ans), cpgen);
        } else if (operand instanceof FSUB) {
            float x;
            float y;
            if (handleX instanceof CPInstruction) {
                x = (float) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (float) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (float) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (float) ((ConstantPushInstruction) handleY).getValue();
            }
            float ans = x - y;
            index = cpgen.addConstant(new ConstantFloat(ans), cpgen);
        } else if (operand instanceof FMUL) {
            float x;
            float y;
            if (handleX instanceof CPInstruction) {
                x = (float) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (float) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (float) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (float) ((ConstantPushInstruction) handleY).getValue();
            }
            float ans = x * y;
            index = cpgen.addConstant(new ConstantFloat(ans), cpgen);
        } else if (operand instanceof FDIV) {
            float x;
            float y;
            if (handleX instanceof CPInstruction) {
                x = (float) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (float) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (float) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (float) ((ConstantPushInstruction) handleY).getValue();
            }
            float ans = x / y;
            index = cpgen.addConstant(new ConstantFloat(ans), cpgen);
        } else if (operand instanceof LADD) {
            long x;
            long y;
            if (handleX instanceof CPInstruction) {
                x = (long) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (long) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (long) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (long) ((ConstantPushInstruction) handleY).getValue();
            }
            long ans = x + y;
            index = cpgen.addConstant(new ConstantLong(ans), cpgen);
        } else if (operand instanceof LSUB) {
            long x;
            long y;
            if (handleX instanceof CPInstruction) {
                x = (long) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (long) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (long) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (long) ((ConstantPushInstruction) handleY).getValue();
            }
            long ans = x - y;
            index = cpgen.addConstant(new ConstantLong(ans), cpgen);
        } else if (operand instanceof LMUL) {
            long x;
            long y;
            if (handleX instanceof CPInstruction) {
                x = (long) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (long) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (long) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (long) ((ConstantPushInstruction) handleY).getValue();
            }
            long ans = x * y;
            index = cpgen.addConstant(new ConstantLong(ans), cpgen);
        } else if (operand instanceof LDIV) {
            long x;
            long y;
            if (handleX instanceof CPInstruction) {
                x = (long) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (long) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (long) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (long) ((ConstantPushInstruction) handleY).getValue();
            }
            long ans = x / y;
            index = cpgen.addConstant(new ConstantLong(ans), cpgen);
        } else if (operand instanceof DADD) {
            double x;
            double y;
            if (handleX instanceof CPInstruction) {
                x = (double) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (double) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (double) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (double) ((ConstantPushInstruction) handleY).getValue();
            }
            double ans = x + y;
            index = cpgen.addConstant(new ConstantDouble(ans), cpgen);
        } else if (operand instanceof DSUB) {
            double x;
            double y;
            if (handleX instanceof CPInstruction) {
                x = (double) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (double) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (double) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (double) ((ConstantPushInstruction) handleY).getValue();
            }
            double ans = x - y;
            index = cpgen.addConstant(new ConstantDouble(ans), cpgen);
        } else if (operand instanceof DMUL) {
            double x;
            double y;
            if (handleX instanceof CPInstruction) {
                x = (double) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (double) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (double) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (double) ((ConstantPushInstruction) handleY).getValue();
            }
            double ans = x * y;
            index = cpgen.addConstant(new ConstantDouble(ans), cpgen);
        } else if (operand instanceof DDIV) {
            double x;
            double y;
            if (handleX instanceof CPInstruction) {
                x = (double) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (double) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (double) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (double) ((ConstantPushInstruction) handleY).getValue();
            }
            double ans = x / y;
            index = cpgen.addConstant(new ConstantDouble(ans), cpgen);
        } else if (operand instanceof LCMP) {
            long x;
            long y;
            if (handleX instanceof CPInstruction) {
                x = (long) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (long) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (long) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (long) ((ConstantPushInstruction) handleY).getValue();
            }
            int ans;
            if (x < y)
                ans = -1;
            else if (x == y)
                ans = 0;
            else
                ans = 1;
            index = cpgen.addConstant(new ConstantInteger(ans), cpgen);
        } else if (operand instanceof FCMPG || operand instanceof FCMPL) {
            float x;
            float y;
            if (handleX instanceof CPInstruction) {
                x = (float) loadDataValue((LDC) handleX, cpgen);
            } else {
                x = (float) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (float) loadDataValue((LDC) handleY, cpgen);
            } else {
                y = (float) ((ConstantPushInstruction) handleY).getValue();
            }
            int ans;
            if (x < y)
                ans = -1;
            else if (x == y)
                ans = 0;
            else
                ans = 1;
            index = cpgen.addConstant(new ConstantInteger(ans), cpgen);
        } else if (operand instanceof DCMPG || operand instanceof DCMPL) {
            double x;
            double y;
            if (handleX instanceof CPInstruction) {
                x = (double) loadDataValue((LDC2_W) handleX, cpgen);
            } else {
                x = (double) ((ConstantPushInstruction) handleX).getValue();
            }
            if (handleY instanceof CPInstruction) {
                y = (double) loadDataValue((LDC2_W) handleY, cpgen);
            } else {
                y = (double) ((ConstantPushInstruction) handleY).getValue();
            }
            int ans;
            if (x < y)
                ans = -1;
            else if (x == y)
                ans = 0;
            else
                ans = 1;
            index = cpgen.addConstant(new ConstantInteger(ans), cpgen);
        }
        return index;
    }

	private boolean foldIfElseStatementBinary(IfInstruction instruction, int x, int y, int index, InstructionHandle[] handles, ConstantPoolGen cpgen, InstructionList instList) {
        boolean flag = false;
        if (instruction instanceof IF_ICMPEQ && x == y)
            flag = true;
        if (instruction instanceof IF_ICMPGE && x >= y)
            flag = true;
        if (instruction instanceof IF_ICMPLE && x <= y)
            flag = true;
        if (instruction instanceof IF_ICMPGT && x > y)
            flag = true;
        if (instruction instanceof IF_ICMPLT && x < y)
            flag = true;
        if (instruction instanceof IF_ICMPNE && x != y)
            flag = true;
        InstructionHandle ifTarget = instruction.getTarget();
        if (findTargetIndex(handles, ifTarget) < index) {
            return false;
        }
        InstructionHandle gotoTarget = null;
        if (ifTarget.getPrev().getInstruction() instanceof GotoInstruction) {
            gotoTarget = ((GotoInstruction) ifTarget.getPrev().getInstruction()).getTarget();
            if (findTargetIndex(handles, gotoTarget) < findTargetIndex(handles, ifTarget) - 1) {
                return false;
            }
        }
        if (flag) {
            setTarget(handles[index - 2], ifTarget, cpgen, instList);
            try {
                instList.delete(handles[index - 2], ifTarget.getPrev());
            } catch (TargetLostException e) {
                System.out.println("target lost");
            }
        } else {
            setTarget(handles[index - 2], handles[index + 1], cpgen, instList);
            try {
                instList.delete(handles[index - 2], handles[index]);
                if (gotoTarget != null) {
                    instList.delete(ifTarget.getPrev(), gotoTarget.getPrev());
                }
            } catch (TargetLostException e) {
                System.out.println("target lost");
            }
        }
        return true;
    }

		

	private void simpleFolding (ConstantPoolGen cpgen, InstructionList instructionList) {
		// fold constants with arithmetic and logic operands (AND OR NOT)
		// In a for-loop, the program scans the list of byte code 
		// instructions and checks if there are constant values 
		
		// (one or two) followed with a recognized operand
	    // Once such instance is found, it will perform operation 
		// corresponding to the operand in the byte code and 
		// replace both the previous values and operand with 
		// a new value. This for-loop will repeat until
		// there is no new places to be optimized.
		// In our implementation, we also handled if-instructions 
		// which follow a constant. As the result can be decided 
		// in that case, we delete the branch which will never 
		// be visited.
		
		InstructionHandle[] handles = instructionList.getInstructionHandles();
        ConstantPool cp = cpgen.getConstantPool();

		boolean isOptimised;

		do {
			isOptimised = true; 
			for (int i=1; i < handles.length ; i++) {

				//unary operand -- unary minus, NOT , INCREMENT, PRE-INCREMENT, DECREMENT, PREDECREMENT

				if (handles[i].getInstruction() instanceof ConversionInstruction || //Super class for the x2y family of instructions (eg I2F)
				/*
				Ref: https://asmsupport.github.io/doc/0.4/jvmref/ref-ineg.html
				*/
				handles[i].getInstruction() instanceof INEG || //Negate int (Pops an int off the stack, negates it, and pushes the negated integer value back onto the stack. This is the same as multiplying the integer by -1.)
				handles[i].getInstruction() instanceof FNEG || // Negate float
				handles[i].getInstruction() instanceof DNEG ||
				handles[i].getInstruction() instanceof LNEG ) {
					if (isLoadOp(handles[i-1].getInstruction())) {
						int idx;
						isOptimised = false;
						if (handles[i-1].getInstruction() instanceof CPInstruction) {
							idx = foldUnaryOperand((CPInstruction) handles[i-1].getInstruction(), handles[i].getInstruction(), cpgen);
						}
						else {
							idx = foldUnaryOperand((ConstantPushInstruction) handles[i - 1].getInstruction(), handles[i].getInstruction(), cpgen);
						}
						Instruction operand = handles[i].getInstruction();
                        if (operand instanceof I2L || operand instanceof F2L || operand instanceof D2L || operand instanceof I2D || operand instanceof F2D || operand instanceof L2D) {
                            instructionList.insert(handles[i], new LDC2_W(idx));
                        } else if (operand instanceof I2F || operand instanceof D2F || operand instanceof L2F || operand instanceof L2I || operand instanceof F2I || operand instanceof D2I) {
                            instructionList.insert(handles[i], new LDC(idx));
                        } else if (operand instanceof INEG || operand instanceof FNEG) {
                            instructionList.insert(handles[i], new LDC(idx));
                        } else if (operand instanceof DNEG || operand instanceof LNEG) {
                            instructionList.insert(handles[i], new LDC2_W(idx));
                        }
						setTarget(i - 1, i, cpgen, instructionList);
                        try {
                            // delete the old ones
                            instructionList.delete(handles[i]);
                            instructionList.delete(handles[i - 1]);
                        } catch (TargetLostException e) {
                            System.out.println("target lost");
                        }
                        break;	
					}
				}
						// conditionals
				if (handles[i].getInstruction() instanceof IFEQ || handles[i].getInstruction() instanceof IFLE
					|| handles[i].getInstruction() instanceof IFGT || handles[i].getInstruction() instanceof IFLT
					|| handles[i].getInstruction() instanceof IFNE || handles[i].getInstruction() instanceof IFGE) {
					
					if (isLoadOp(handles[i].getInstruction())) {
						int value ;
						if (handles[i].getInstruction() instanceof ConstantPushInstruction) {
							value = (int) ((ConstantPushInstruction) handles[i-1].getInstruction()).getValue();
						}
						else {
							value = (int) ((LDC) handles[i-1].getInstruction()).getValue(cpgen);
						}
						if (foldIfElseStatementUnary(handles[i],value,i,handles,cpgen,instructionList)) {
							isOptimised = false;
						}
						break;
					}
				}

				// binary operand 
				if (i==1) {
					continue;
				}
				if (handles[i].getInstruction() instanceof ArithmeticInstruction || handles[i].getInstruction() instanceof LCMP
                        || handles[i].getInstruction() instanceof FCMPG || handles[i].getInstruction() instanceof FCMPL // compare
                        || handles[i].getInstruction() instanceof DCMPG || handles[i].getInstruction() instanceof DCMPL) {
                    if (handles[i - 1].getInstruction() instanceof PushInstruction
                            && handles[i - 2].getInstruction() instanceof PushInstruction //Denotes an unparameterized instruction to produce a value on top of the stack
                            && !(handles[i - 1].getInstruction() instanceof LoadInstruction) //Denotes an unparameterized instruction to load a value from a local variable
                            && !(handles[i - 2].getInstruction() instanceof LoadInstruction)
                            ) {
                        isOptimised = false;
                        int index = calc((PushInstruction) handles[i - 2].getInstruction(), (PushInstruction) handles[i - 1].getInstruction(), handles[i].getInstruction(), cpgen);
                        Instruction operand = handles[i].getInstruction();
                        if (operand instanceof IADD || operand instanceof ISUB || operand instanceof IMUL || operand instanceof IDIV || operand instanceof FADD || operand instanceof FSUB || operand instanceof FMUL || operand instanceof FDIV
                                || operand instanceof LCMP || operand instanceof FCMPL || operand instanceof FCMPG || operand instanceof DCMPL || operand instanceof DCMPG) {
									instructionList.insert(handles[i], new LDC(index));
                        } else {
                            instructionList.insert(handles[i], new LDC2_W(index));
                        }
                        setTarget(i - 2, i, cpgen, instructionList);
                        try {
                            // delete the old ones
                            instructionList.delete(handles[i]);
                            instructionList.delete(handles[i - 1]);
                            instructionList.delete(handles[i - 2]);
                        } catch (TargetLostException e) {
                            System.out.println("target lost");
                        }
                        break;
					}
				}

				/*
				Both value1 and value2 must be of type int. They are both popped from the operand stack and compared. All comparisons are signed. The results of the comparison are as follows:

					if_icmpeq succeeds if and only if value1 = value2
					if_icmpne succeeds if and only if value1 ≠ value2
					if_icmplt succeeds if and only if value1 < value2
					if_icmple succeeds if and only if value1 ≤ value2
					if_icmpgt succeeds if and only if value1 > value2
					if_icmpge succeeds if and only if value1 ≥ value2
				*/
                if (handles[i].getInstruction() instanceof IF_ICMPEQ || handles[i].getInstruction() instanceof IF_ICMPGE
                        || handles[i].getInstruction() instanceof IF_ICMPGT || handles[i].getInstruction() instanceof IF_ICMPLE
                        || handles[i].getInstruction() instanceof IF_ICMPLT || handles[i].getInstruction() instanceof IF_ICMPNE) {
                    if ((handles[i - 1].getInstruction() instanceof LDC || handles[i - 1].getInstruction() instanceof ICONST)
                            && (handles[i - 2].getInstruction() instanceof LDC || handles[i - 2].getInstruction() instanceof ICONST)) {
                        int x, y;
                        if (handles[i - 1].getInstruction() instanceof ConstantPushInstruction){
                            y = (int) ((ConstantPushInstruction) handles[i - 1].getInstruction()).getValue();
						}
                        else {
                            y = (int) ((LDC) handles[i - 1].getInstruction()).getValue(cpgen);
						}
                        if (handles[i - 2].getInstruction() instanceof ConstantPushInstruction) {
                            x = (int) ((ConstantPushInstruction) handles[i - 2].getInstruction()).getValue();
						}
						else {
                            x = (int) ((LDC) handles[i - 2].getInstruction()).getValue(cpgen);
                        } 
						if (foldIfElseStatementBinary((IfInstruction) handles[i].getInstruction(), x, y, i, handles, cpgen, instructionList)) {
                            isOptimised = false;
						}
                        break;
					}
				}
			}
			handles = instructionList.getInstructionHandles();
		} while (!isOptimised);
	

	}

	private Method optimizeMethod(ClassGen cgen, Method m) {
        ConstantPoolGen cpgen = cgen.getConstantPool();
        MethodGen mg = new MethodGen(m, cgen.getClassName(), cpgen);
        mg.removeNOPs();
        ////Remove all NOPs  (dofrom the instruction list (if possible) and update every object referring to them, i.e., branch instructions, local variables and exception handlers.

        InstructionList instList = mg.getInstructionList();

        simpleFolding(cpgen, instList);

        // constantVariables(cpgen, instList);

        // dynamicVariables(cpgen, instList);

        mg.stripAttributes(true);
        mg.setMaxStack();
        return mg.getMethod();
    }
	
	public void optimize()
	{
        ClassGen cgen = new ClassGen(original);
        ConstantPoolGen cpgen = cgen.getConstantPool();
		cgen.setMajor(50);

        Method[] methods = cgen.getMethods();
        int length = methods.length;
        Method[] optimizedMethods = new Method[length];
        System.out.printf("*******%s*********\n",cgen.getClassName());
        for (int i = 0; i < length; i++) {
            System.out.println("......................optimizing method: " + i + methods[i]);
            optimizedMethods[i] = optimizeMethod(cgen, methods[i]);
        }
        this.gen.setMethods(optimizedMethods);
        this.gen.setConstantPool(cpgen);
        this.gen.setMajor(50);

        this.optimized = cgen.getJavaClass();
	}
	
	
	
	
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
}