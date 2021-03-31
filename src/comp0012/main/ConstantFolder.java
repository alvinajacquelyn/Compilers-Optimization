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
        // System.out.println(instructionList.toString());

		boolean isOptimised;

		do {
			isOptimised = true; 
			for (int i=1; i < handles.length ; i++) {


                // Const Var method 1, 2, 4 and Dynamic Var method 1, 3, 4 and Simple folding simple() passes through below loop
				if (handles[i].getInstruction() instanceof ArithmeticInstruction || handles[i].getInstruction() instanceof LCMP
                        || handles[i].getInstruction() instanceof FCMPG || handles[i].getInstruction() instanceof FCMPL // compare
                        || handles[i].getInstruction() instanceof DCMPG || handles[i].getInstruction() instanceof DCMPL) {

                    // Simple Folding simple() method passes through below IF loop
                    if (handles[i - 1].getInstruction() instanceof PushInstruction
                            && handles[i - 2].getInstruction() instanceof PushInstruction //Denotes an unparameterized instruction to produce a value on top of the stack
                            && !(handles[i - 1].getInstruction() instanceof LoadInstruction) //Denotes an unparameterized instruction to load a value from a local variable
                            && !(handles[i - 2].getInstruction() instanceof LoadInstruction)
                            ) {
                    
                        isOptimised = false;
                        int index = calc((PushInstruction) handles[i - 2].getInstruction(), (PushInstruction) handles[i - 1].getInstruction(), handles[i].getInstruction(), cpgen);
                        Instruction operand = handles[i].getInstruction();
                        System.out.println("Instruction operand = handles[i].getInstruction();  : " + operand);

                        if (operand instanceof IADD || operand instanceof ISUB || operand instanceof IMUL || operand instanceof IDIV || operand instanceof FADD || operand instanceof FSUB || operand instanceof FMUL || operand instanceof FDIV
                                || operand instanceof LCMP || operand instanceof FCMPL || operand instanceof FCMPG || operand instanceof DCMPL || operand instanceof DCMPG) {
                                    System.out.println("i: " + i);
                                    System.out.println("handles[i]:   "+handles[i]);
                                    System.out.println("handles[i-1]: "+handles[i-1]);
                                    System.out.println("handles[i-2]: "+handles[i-2]);
                                    // LDC loads int, float, String, Class from the constant pool onto the stac
                                    instructionList.insert(handles[i], new LDC(index));
                        } else { // LDC2_W loads long or double type into the stack
                            instructionList.insert(handles[i], new LDC2_W(index));
                        }
                        // setTarget(i - 2, i, cpgen, instructionList);
                        try {
                            // delete the old ones
                            /*
                            [java] Deleting    7: iadd[96](1)
                            [java] Deleting    5: ldc[18](2) 8
                            [java] Deleting    3: ldc[18](2) 24
                            */
                           System.out.println("Deleting " + handles[i]);
                            instructionList.delete(handles[i]);
                            System.out.println("Deleting " + handles[i-1]);
                            instructionList.delete(handles[i - 1]);
                            System.out.println("Deleting " + handles[i-2]);
                            instructionList.delete(handles[i - 2]);
                        } catch (TargetLostException e) {
                            System.out.println("target lost");
                        }
                        break;
					}
				}
			}
            System.out.println(isOptimised);
			handles = instructionList.getInstructionHandles();
		} while (!isOptimised);
	}

	private Method optimizeMethod(ClassGen cgen, Method m) {
        ConstantPoolGen cpgen = cgen.getConstantPool();
        MethodGen mg = new MethodGen(m, cgen.getClassName(), cpgen);
        mg.removeNOPs();
        //Remove all NOPs  (dofrom the instruction list (if possible) and update every object referring to them, i.e., branch instructions, local variables and exception handlers.
        InstructionList instList = mg.getInstructionList();

        // System.out.println("**********************************");
        // System.out.println("******Instructions before: *******");
        // System.out.println("**************************Count: "+instList.getLength()); 
		// System.out.println(instList.toString());
        // System.out.println("**********************************");

        simpleFolding(cpgen, instList);

        // constantVariables(cpgen, instList);

        // dynamicVariables(cpgen, instList);

        mg.stripAttributes(true);
        mg.setMaxStack();
        // System.out.println("******Instructions after**********");
        // System.out.println("**************************Count: "+instList.getLength()); 
		// System.out.println(instList.toString());

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
            System.out.println("...............optimizing method: " + i +" " + methods[i]);
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