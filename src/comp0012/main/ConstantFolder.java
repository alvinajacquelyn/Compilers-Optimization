package comp0012.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;


public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

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

	/*
	 * GETTING THE LOADVALUES
	*/
	private int getLoadIntValue (InstructionHandle handle, InstructionList instList, ConstantPoolGen  cpgen, int load_index)
	{
		// get the int value
		// iterate back until the value is found (where the instruction index for the ISTORE is the same as ILOAD)
		
		// start from the current handle
		InstructionHandle newHandle = handle;
		
		//iterate back
		while (newHandle.getPrev()!=null)
		{
			if (newHandle.getInstruction() instanceof ISTORE)
			{
				if ((load_index == (int)((ISTORE)(newHandle.getInstruction())).getIndex())) 
				{
					int v = getPrevInt(newHandle.getPrev(), instList, cpgen);
					return v;
				}
			}		
			newHandle = newHandle.getPrev();
		}
		return 0;
	}

	private long getLoadLongValue (InstructionHandle handle, InstructionList instList, ConstantPoolGen  cpgen, int load_index)
	{
		// get the long value
		// iterate back until the value is found (where the instruction index for the LSTORE is the same as LLOAD)
		
		// start from the current handle
		InstructionHandle newHandle = handle;
		
		//iterate back
		while (newHandle.getPrev()!=null)
		{
			if (newHandle.getInstruction() instanceof LSTORE)
			{
				if ((load_index == (int)((LSTORE)(newHandle.getInstruction())).getIndex())) 
				{
					return getPrevLong(newHandle.getPrev(), instList, cpgen);
				}
			}		
			newHandle = newHandle.getPrev();
		}
		return 0;
	}

	private double getLoadDoubleValue (InstructionHandle handle, InstructionList instList, ConstantPoolGen  cpgen, int load_index)
	{
		// get the double value
		// iterate back until the value is found (where the instruction index for the DSTORE is the same as DLOAD)
		
		// start from the current handle
		InstructionHandle newHandle = handle;
		
		//iterate back
		while (newHandle.getPrev()!=null)
		{
			if (newHandle.getInstruction() instanceof DSTORE)
			{
				if ((load_index == (int)((DSTORE)(newHandle.getInstruction())).getIndex())) 
				{
					return getPrevDouble(newHandle.getPrev(), instList, cpgen);
				}
			}		
			newHandle = newHandle.getPrev();
		}
		return 0;
	}

	private float getLoadFloatValue (InstructionHandle handle, InstructionList instList, ConstantPoolGen  cpgen, int load_index)
	{
		// get the flaot value
		// iterate back until the value is found (where the instruction index for the FSTORE is the same as FLOAD)
		
		// start from the current handle
		InstructionHandle newHandle = handle;
		
		//iterate back
		while (newHandle.getPrev()!=null)
		{
			if (newHandle.getInstruction() instanceof FSTORE)
			{
				if ((load_index == (int)((FSTORE)(newHandle.getInstruction())).getIndex())) 
				{
					return getPrevFloat(newHandle.getPrev(), instList, cpgen);
				}
			}		
			newHandle = newHandle.getPrev();
		}
		return 0;
	}
	

	
	/**
	 * GET PREVIOUS VALUES 
	**/
	/** ****************************************************************************************************************** **/
	private int getPrevInt(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof ICONST)
		{
			return (int) ((ICONST)(handle.getInstruction())).getValue();
		} else if (handle.getInstruction() instanceof BIPUSH)
		{
			return (int) ((BIPUSH)(handle.getInstruction())).getValue();
		} else if (handle.getInstruction() instanceof SIPUSH)
		{
			return (int) ((SIPUSH)(handle.getInstruction())).getValue();
		} else if (handle.getInstruction() instanceof LDC)
		{
			return (int) ((LDC)(handle.getInstruction())).getValue(cpgen);
		} else if (handle.getInstruction() instanceof ILOAD)
		{
			int load_index = (int) ((ILOAD)(handle.getInstruction())).getIndex();
        	int value = getLoadIntValue(handle, instList,cpgen,load_index);
        	return value;
		} else if (handle.getInstruction() instanceof D2I)
		{
			int value =  (int) getPrevDouble (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
				} catch (TargetLostException e) {
					e.printStackTrace();
					}
			return value;
		} else if (handle.getInstruction() instanceof F2I)
		{
			int value =  (int) getPrevFloat (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
				} catch (TargetLostException e) {
					e.printStackTrace();
					}
			return value;
		} else if (handle.getInstruction() instanceof L2I)
		{
			int value =  (int) getPrevLong (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
				} catch (TargetLostException e) {
					e.printStackTrace();
					}
			return value;
		} else 
		{
			return 0;
		}
	}

	private long getPrevLong(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LCONST)
		{
			return (long) ((LCONST)(handle.getInstruction())).getValue();
		} else if (handle.getInstruction() instanceof LDC2_W)
		{
			return (long) ((LDC2_W)(handle.getInstruction())).getValue(cpgen);
		} else if (handle.getInstruction() instanceof LLOAD)
		{
			int load_index = (int) ((LLOAD)(handle.getInstruction())).getIndex();
        	long value = getLoadLongValue(handle, instList,cpgen,load_index);
        	return value;
		} else if (handle.getInstruction() instanceof F2L)
		{
			long value =  (long) getPrevFloat (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
				} catch (TargetLostException e) {
					e.printStackTrace();
				}
			return value;
		} else if (handle.getInstruction() instanceof D2L)
		{
			long value =  (long) getPrevDouble (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
			} catch (TargetLostException e) {
				e.printStackTrace();
			}
			return value;
		} else if (handle.getInstruction() instanceof I2L)
		{
			long value =  (long) getPrevInt (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
			} catch (TargetLostException e) {
				e.printStackTrace();
			}
			return value;
		}else {
			return 0;
		}
	}

	private double getPrevDouble(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof DCONST)
		{
			return (double) ((DCONST)(handle.getInstruction())).getValue();
		} else if (handle.getInstruction() instanceof LDC2_W)
		{
			return (double) ((LDC2_W)(handle.getInstruction())).getValue(cpgen);
		} else if (handle.getInstruction() instanceof DLOAD)
		{
			int load_index = (int) ((DLOAD)(handle.getInstruction())).getIndex();
        	double value = getLoadDoubleValue(handle, instList,cpgen,load_index);
        	return value;
		} else if (handle.getInstruction() instanceof I2D)
		{
			double value =  (double) getPrevInt (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
			} catch (TargetLostException e) {
				e.printStackTrace();
			}
			return value;
		} else if (handle.getInstruction() instanceof F2D)
		{
			double value =  (double) getPrevFloat (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
				} catch (TargetLostException e) {
					e.printStackTrace();
				}
			return value;
		} else if (handle.getInstruction() instanceof L2D)
		{
			double value =  (double) getPrevLong (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
			} catch (TargetLostException e) {
				e.printStackTrace();
			}
			return value;
		} else {
			return 0;
		}
	}

	private float getPrevFloat(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof FCONST)
		{
			return (float) ((FCONST)(handle.getInstruction())).getValue();
		} else if (handle.getInstruction() instanceof LDC)
		{
			return (float) ((LDC)(handle.getInstruction())).getValue(cpgen);
		} else if (handle.getInstruction() instanceof FLOAD)
		{
			int load_index = (int) ((FLOAD)(handle.getInstruction())).getIndex();
        	float value = getLoadFloatValue(handle, instList,cpgen,load_index);
        	return value;
		} else if (handle.getInstruction() instanceof I2F)
		{
			float value =  (float) getPrevInt (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
			} catch (TargetLostException e) {
				e.printStackTrace();
			}
			return value;
		} else if (handle.getInstruction() instanceof D2F)
		{
			float value =  (float) getPrevDouble (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
				} catch (TargetLostException e) {
					e.printStackTrace();
				}
			return value;
		} else if (handle.getInstruction() instanceof L2F)
		{
			float value =  (float) getPrevLong (handle.getPrev(), instList, cpgen);
			try {
				instList.delete(handle.getPrev());
			} catch (TargetLostException e) {
				e.printStackTrace();
			}
			return value;
		}else {
			return 0;
		}
	}

	//delete handles
	private void delete_handles(InstructionList instList, InstructionHandle handle, InstructionHandle handle_to_delete_1, InstructionHandle handle_to_delete_2) 
	{
		if(handle!=null)
		{
			try {
				 instList.delete(handle);
			 } catch (TargetLostException e) {
				 e.printStackTrace();
			 }
		}
		
		if(handle_to_delete_1!=null)
		{
			try {
				 instList.delete(handle_to_delete_1);
			 } catch (TargetLostException e) {
				 e.printStackTrace();
			 }
		}
		
		if(handle_to_delete_2!=null)
		{
			try {
				 instList.delete(handle_to_delete_2);
			 } catch (TargetLostException e) {
				 e.printStackTrace();
			 }
		}
	}

	// optimise arithmetic operations
	private void optimizeArithmetic (InstructionHandle handle, InstructionList instList, ClassGen cgen, ConstantPoolGen cpgen)
	{ 
		//optimising arithmetic for integers
		if (handle.getInstruction() instanceof IADD || handle.getInstruction() instanceof ISUB || handle.getInstruction() instanceof IMUL ||handle.getInstruction() instanceof IDIV || handle.getInstruction() instanceof IREM)
		{
			InstructionHandle handle_to_delete_1 = handle.getPrev(); 
			InstructionHandle handle_to_delete_2 = handle.getPrev().getPrev();
			//Searching for the values
			int value1 = getPrevInt(handle.getPrev(), instList, cpgen);
			int value2 = getPrevInt(handle.getPrev().getPrev(), instList, cpgen);
			int val = 0;
			if (handle.getInstruction() instanceof IADD){val = value1+value2;}
			if (handle.getInstruction() instanceof ISUB){val = value2-value1;}
			if (handle.getInstruction() instanceof IMUL){val = value2*value1;}
			if (handle.getInstruction() instanceof IDIV){val = value2/value1;}
			if (handle.getInstruction() instanceof IREM){val = value2%value1;}
			
        	//adding the values - LDC pushes the int value onto the stack 
        	instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(val)));
        	if(handle_to_delete_1.getInstruction() instanceof D2I || handle_to_delete_1.getInstruction() instanceof F2I || handle_to_delete_1.getInstruction() instanceof L2I)
			{
				delete_handles(instList, handle, handle_to_delete_1, null);
			} else{
				delete_handles(instList, handle, handle_to_delete_1, handle_to_delete_2);
			}
		}
		//optimising arithmetic for longs
		if (handle.getInstruction() instanceof LADD || handle.getInstruction() instanceof LSUB || handle.getInstruction() instanceof LMUL ||handle.getInstruction() instanceof LDIV || handle.getInstruction() instanceof LREM)
		{
			InstructionHandle handle_to_delete_1 = handle.getPrev(); 
			InstructionHandle handle_to_delete_2 = handle.getPrev().getPrev();
			//Searching for the values
			long value1 = getPrevLong(handle.getPrev(), instList, cpgen);
			long value2 = getPrevLong(handle.getPrev().getPrev(), instList, cpgen);
			long val = 0;
			if (handle.getInstruction() instanceof LADD){val = value1+value2;}
			if (handle.getInstruction() instanceof LSUB){val = value2-value1;}
			if (handle.getInstruction() instanceof LMUL){val = value2*value1;}
			if (handle.getInstruction() instanceof LDIV){val = value2/value1;}
			if (handle.getInstruction() instanceof LREM){val = value2%value1;}  
			
		    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(val)));
		    if(handle_to_delete_1.getInstruction() instanceof I2L || handle_to_delete_1.getInstruction() instanceof F2L || handle_to_delete_1.getInstruction() instanceof D2L)
			{
				delete_handles(instList, handle, handle_to_delete_1, null);
			} else{
				delete_handles(instList, handle, handle_to_delete_1, handle_to_delete_2);
			}
		}

		//optimising arithmetic for Floats
		if (handle.getInstruction() instanceof FADD || handle.getInstruction() instanceof FSUB || handle.getInstruction() instanceof FMUL ||handle.getInstruction() instanceof FDIV || handle.getInstruction() instanceof FREM)
		{
			InstructionHandle handle_to_delete_1 = handle.getPrev(); 
			InstructionHandle handle_to_delete_2 = handle.getPrev().getPrev();
			//Searching for the values
			float value1 = getPrevFloat(handle.getPrev(), instList, cpgen);
			float value2 = getPrevFloat(handle.getPrev().getPrev(), instList, cpgen);
			float val = 0;
			if (handle.getInstruction() instanceof FADD){val = value1+value2;}
			if (handle.getInstruction() instanceof FSUB){val = value2-value1;}
			if (handle.getInstruction() instanceof FMUL){val = value2*value1;}
			if (handle.getInstruction() instanceof FDIV){val = value2/value1;}
			if (handle.getInstruction() instanceof FREM){val = value2%value1;}	
			
		    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(val)));
		    if(handle_to_delete_1.getInstruction() instanceof I2F || handle_to_delete_1.getInstruction() instanceof D2F || handle_to_delete_1.getInstruction() instanceof L2F)
			{	
				delete_handles(instList, handle, handle_to_delete_1, null);
			} else {
				delete_handles(instList, handle ,handle_to_delete_1, handle_to_delete_2);
			}
		}

		//optimising arithmetic for doubles
		if (handle.getInstruction() instanceof DADD || handle.getInstruction() instanceof DSUB || handle.getInstruction() instanceof DMUL ||handle.getInstruction() instanceof DDIV || handle.getInstruction() instanceof DREM)
		{
			InstructionHandle handle_to_delete_1 = handle.getPrev(); 
			InstructionHandle handle_to_delete_2 = handle.getPrev().getPrev();
			//Searching for the values
			double value1 = getPrevDouble(handle.getPrev(), instList, cpgen);
			double value2 = getPrevDouble(handle.getPrev().getPrev(), instList, cpgen);
			double val = 0;
			if (handle.getInstruction() instanceof DADD){val = value1+value2;}
			if (handle.getInstruction() instanceof DSUB){val = value2-value1;}
			if (handle.getInstruction() instanceof DMUL){val = value2*value1;}
			if (handle.getInstruction() instanceof DDIV){val = value2/value1;}
			if (handle.getInstruction() instanceof DREM){val = value2%value1;} 
			
		    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(val)));			
			if (handle_to_delete_1.getInstruction() instanceof I2D || handle_to_delete_1.getInstruction() instanceof F2D || handle_to_delete_1.getInstruction() instanceof L2D)
			{
				delete_handles(instList, handle,handle_to_delete_1, null);
			} else {
				delete_handles(instList, handle,handle_to_delete_1, handle_to_delete_2);
			}
		}
	}
	

	private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method) {
		// Get the Code of the method, which is a collection of bytecode instructions
		Code methodCode = method.getCode();

		// Now get the actually bytecode data in byte array,and use it to initialise an InstructionList
		InstructionList instList = new InstructionList(methodCode.getCode());

		/*
		The class InstructionList is a container for a list of Instruction
		objects. Instruction can be appended, inserted, deleted, etc.
		Each Instruction is wrapped into a InstructionHandles object
		that provides the user with access capability to the instruction
		list structure that can easily be traversed.
		http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.676.3877&rep=rep1&type=pdf
		*/

		// Initialise a method generator with the original method as the baseline
		MethodGen methodGen = new MethodGen(method, cgen.getClassName(), cpgen);        
		Method newMethod = methodGen.getMethod();
        System.out.println("**********************************");
        System.out.println("******Instructions before: *******");
        System.out.println("**************************Count: "+instList.getLength()); 
		System.out.println(instList.toString());
        System.out.println("**********************************");
		//<name of opcode> "["<opcode number>"]" "("<length of instruction>")"
		// InstructionHandle is a wrapper for actual Instructions
		for (InstructionHandle handle : instList.getInstructionHandles()) {
			optimizeArithmetic(handle, instList, cgen, cpgen);	            
			// optimizeComparisons(handle, instList, cgen, cpgen);
			
			// set max stack/local
			methodGen.setMaxStack();
			methodGen.setMaxLocals();
				
			methodGen.setInstructionList(instList);
				
			// remove local variable table
			methodGen.removeLocalVariables();
	
			// generate the new method with replaced instList
			newMethod = methodGen.getMethod();
			// replace the method in the original class
			cgen.replaceMethod(method, newMethod);
		}
		System.out.println("******Instructions after**********");
		System.out.println("**************************Count: "+instList.getLength()); 
		System.out.println(instList.toString());
	}

	
	
	public void optimize()
	{
		/*
		The org.apache.bcel.generic package denes classes whose instances deal with the construction of various class components
		and the dynamic transformation of class le.
		The generic constant pool, for example, implemented by the
		class ConstantPoolGen, provides with methods for introducing
		new constant types.
		Accordingly, ClassGen provides with routines to add or delete
		methods, elds, and class attributes.
		In short, ClassGen objects represent classes that can be
		edited. You can add methods and elds, you can modify existing methods, and so on.

		*/
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Implement your optimization here
		System.out.printf("*******%s*********\n",cgen.getClassName());

        Method[] methods = cgen.getMethods();
		for (Method m : methods) {
			optimizeMethod(cgen, cpgen, m);
		}
	    cgen.setMajor(50);

		this.optimized = gen.getJavaClass();
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