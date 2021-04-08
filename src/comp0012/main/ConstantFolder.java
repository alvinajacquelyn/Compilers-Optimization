package comp0012.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Iterator;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.InstructionFinder;
import org.hamcrest.core.IsInstanceOf;

import java_cup.runtime.double_token;
import java_cup.runtime.float_token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
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


	
	/**
	 * GET PREVIOUS VALUES 
	**/
	/** ****************************************************************************************************************** **/
	//This will also recognize BIPUSH and SIPUSH int values as well as ICONST values
	private int getPrevInt(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LDC)
		{
			return (int) ((LDC)(handle.getInstruction())).getValue(cpgen);
		} 
		else if (handle.getInstruction() instanceof BIPUSH) 
		{
			return (int) ((BIPUSH)(handle.getInstruction())).getValue();
		} 
		else if (handle.getInstruction() instanceof SIPUSH) 
		{
			return (int) ((SIPUSH)(handle.getInstruction())).getValue();
		}
		else if (handle.getInstruction() instanceof ICONST)
		{
			return (int) ((ICONST) handle.getInstruction()).getValue();
		}
		else
		{
			return 0;
		}
	}

	private long getPrevLong(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LDC2_W)
		{
			return (long) ((LDC2_W)(handle.getInstruction())).getValue(cpgen);

		}	else {
			return 0;
		}
	}

	private double getPrevDouble(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LDC2_W)
		{
			return (double) ((LDC2_W)(handle.getInstruction())).getValue(cpgen);
		
		} else {
			return 0;
		}
	}

	private float getPrevFloat(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (handle.getInstruction() instanceof LDC)
		{
			return (float) ((LDC)(handle.getInstruction())).getValue(cpgen);

		}else {
			return 0;
		}
	}

	//delete handles
	private void delete_handles(InstructionList instList, InstructionHandle handle, InstructionHandle handle_to_delete_1, InstructionHandle handle_to_delete_2) 
	{

		//delete the stack operation and two pushes which will
		//generates the result as a constant #index on the constant pool
		//replaced with a single instruction, to be loaded (ldc) from constant pool on to the stack
		
		try {
				instList.delete(handle);
			} catch (TargetLostException e) {
				e.printStackTrace();
			}


		try {
				instList.delete(handle_to_delete_1);
			} catch (TargetLostException e) {
				e.printStackTrace();
			}

		try {
				instList.delete(handle_to_delete_2);
			} catch (TargetLostException e) {
				e.printStackTrace();
			}

	}

	// optimise arithmetic operations
	/*
	iterates through the instructions until it finds a stack operation, w
	then runs on the last two items pushed onto the stack
	*/
	private void optimizeArithmetic (InstructionHandle handle, InstructionList instList, ClassGen cgen, ConstantPoolGen cpgen)
	{ 
		//optimising arithmetic for integers
		if (handle.getInstruction() instanceof IADD || handle.getInstruction() instanceof ISUB || handle.getInstruction() instanceof IMUL ||handle.getInstruction() instanceof IDIV)
		{
			System.out.println(".............optimising arithmetic for integer");
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
			
        	//adding the values - LDC pushes the int value onto the stack 
        	instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(val)));

			delete_handles(instList, handle, handle_to_delete_1, handle_to_delete_2);

		}
		//optimising arithmetic for longs
		if (handle.getInstruction() instanceof LADD || handle.getInstruction() instanceof LSUB || handle.getInstruction() instanceof LMUL ||handle.getInstruction() instanceof LDIV)
		{
			System.out.println(".............optimising arithmetic for longs");
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
			
		    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(val)));

			delete_handles(instList, handle, handle_to_delete_1, handle_to_delete_2);

		}

		//optimising arithmetic for Floats
		if (handle.getInstruction() instanceof FADD || handle.getInstruction() instanceof FSUB || handle.getInstruction() instanceof FMUL ||handle.getInstruction() instanceof FDIV)
		{
			System.out.println(".............optimising arithmetic for floats");

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
			
		    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(val)));

			delete_handles(instList, handle ,handle_to_delete_1, handle_to_delete_2);

		}

		//optimising arithmetic for doubles
		if (handle.getInstruction() instanceof DADD || handle.getInstruction() instanceof DSUB || handle.getInstruction() instanceof DMUL ||handle.getInstruction() instanceof DDIV )
		{
			System.out.println(".............optimising arithmetic for doubles");
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
			
		    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(val)));			

			delete_handles(instList, handle,handle_to_delete_1, handle_to_delete_2);
		}
	}


	private void optimizeComparisons (InstructionHandle handle, InstructionList instList, ClassGen cgen, ConstantPoolGen cpgen)
	{ 
		
		//optimising comparisons for integers
		if (handle.getInstruction() instanceof IF_ICMPEQ || handle.getInstruction() instanceof IF_ICMPNE || handle.getInstruction() instanceof IF_ICMPLT || handle.getInstruction() instanceof IF_ICMPGE || handle.getInstruction() instanceof IF_ICMPGT || handle.getInstruction() instanceof IF_ICMPLE)
		{
			System.out.println(".............optimising comparisons for integer");
			InstructionHandle handle1 = handle.getPrev(); 
			InstructionHandle handle2 = handle.getPrev().getPrev();

			//Searching for the values
			Integer value1 = getPrevInt(handle.getPrev(), instList, cpgen);
			Integer value2 = getPrevInt(handle.getPrev().getPrev(), instList, cpgen);
			boolean val = false;
			if (handle.getInstruction() instanceof IF_ICMPEQ){val = value1.equals(value2);}
			if (handle.getInstruction() instanceof IF_ICMPNE){val = !value1.equals(value2);}
			if (handle.getInstruction() instanceof IF_ICMPLT){val = (value1.compareTo(value2) < 0);}
			if (handle.getInstruction() instanceof IF_ICMPGE){val = (value1.compareTo(value2) >= 0);}
			if (handle.getInstruction() instanceof IF_ICMPGT){val = (value1.compareTo(value2) > 0);}
			if (handle.getInstruction() instanceof IF_ICMPLE){val = (value1.compareTo(value2) <= 0);}
			
			//removes the prior iloads and IF_ICMP<cond> instructions from the stack - and pushes a GOTO instead
			if (val) {
				handle.setInstruction(new GOTO(((IfInstruction) handle.getInstruction()).getTarget()));
				instList.redirectBranches(handle2, handle);
				try {
					instList.delete(handle2, handle1);
				} catch (TargetLostException e) {
					e.printStackTrace();
				}
			} else {
				instList.redirectBranches(handle2, handle.getNext());
				try {
					instList.delete(handle2, handle);
				} catch (TargetLostException e) {
					e.printStackTrace();
				}
			}

		}
		
		//optimising comparisons for longs
		if (handle.getInstruction() instanceof LCMP)
		{
			System.out.println(".............optimising comparisons for longs");
			InstructionHandle handle_to_delete_1 = handle.getPrev(); 
			InstructionHandle handle_to_delete_2 = handle.getPrev().getPrev();
			//Searching for the values
			long value1 = getPrevLong(handle.getPrev(), instList, cpgen);
			long value2 = getPrevLong(handle.getPrev().getPrev(), instList, cpgen);
			long val = 0;
			if (handle.getInstruction() instanceof LCMP){val = (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));}
			
		    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(val)));

			delete_handles(instList, handle, handle_to_delete_1, handle_to_delete_2);

		}

		//optimising comparison for Floats
		if (handle.getInstruction() instanceof FCMPG || handle.getInstruction() instanceof FCMPL)
		{
			System.out.println(".............optimising comparisons for floats");

			InstructionHandle handle_to_delete_1 = handle.getPrev(); 
			InstructionHandle handle_to_delete_2 = handle.getPrev().getPrev();
			//Searching for the values
			float value1 = getPrevFloat(handle.getPrev(), instList, cpgen);
			float value2 = getPrevFloat(handle.getPrev().getPrev(), instList, cpgen);
			float val = 0;
			if (handle.getInstruction() instanceof FCMPG){val = (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));}
			if (handle.getInstruction() instanceof FCMPL){val = (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));}
			
		    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(val)));

			delete_handles(instList, handle ,handle_to_delete_1, handle_to_delete_2);

		}

		//optimising comparisons for doubles
		if (handle.getInstruction() instanceof DCMPG || handle.getInstruction() instanceof DCMPL)
		{
			System.out.println(".............optimising comparisons for doubles");
			InstructionHandle handle_to_delete_1 = handle.getPrev(); 
			InstructionHandle handle_to_delete_2 = handle.getPrev().getPrev();
			//Searching for the values
			double value1 = getPrevDouble(handle.getPrev(), instList, cpgen);
			double value2 = getPrevDouble(handle.getPrev().getPrev(), instList, cpgen);
			double val = 0;
			if (handle.getInstruction() instanceof DCMPG){val = (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));}
			if (handle.getInstruction() instanceof DCMPL){val = (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));}
			
		    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(val)));			

			delete_handles(instList, handle,handle_to_delete_1, handle_to_delete_2);
		}
	}
	
	// ---------------------------------------------------------------------
	// ----------------OPTIMIZE CONSTANTS + DYNAMIC VARS -------------------
	private void optimiseVariables(Method method, InstructionList instList, ClassGen cgen, ConstantPoolGen cpgen)
	{
		// Optimises for:  int, long ,float and double
		// HashSet<Integer> localVarIndices = getLocalVariableIndices(instList);
		// LocalVariable[] localVariables = method.getLocalVariableTable().getLocalVariableTable();
		// for (LocalVariable localVariable : localVariables) {
		// 	System.out.println(localVariable);
		// }

		Hashtable<String, Object> variables = new Hashtable<String, Object>();
		// Stores current values of all variables, where the key is the variable type + index
		// e.g. I1 = 23; L2 = 2323232323434
		for (InstructionHandle iHandle : instList.getInstructionHandles()) {
			if(handleVariableStores(iHandle, variables, instList, cpgen))
			{
				continue;
			}
			handleVariableLoads(iHandle, variables, instList, cpgen);
		}
		System.out.println(variables);
	}

	private boolean handleVariableStores(InstructionHandle iHandle, Hashtable<String, Object> variables, InstructionList instList, ConstantPoolGen cpgen)
	{
		// TODO Might need to add an option for IINC
		// Takes care of modifying values of current variables if they can be "decided", returns true if a storing instruction occurs to ommit unnecessary calculations
		// INTEGERS
		if (iHandle.getInstruction() instanceof ISTORE)
		{
			ISTORE var = ((ISTORE) iHandle.getInstruction());
			String key = "I"+var.getIndex();
			Object val = ValueGetter.getPrevInt(iHandle.getPrev(), instList, cpgen);
			if (val != null)
			{
				variables.put(key, (int) val);
			}
			else if (variables.get(key) != null)
			{
				variables.remove(key);
			}
			// if (variables.get(key) != null)
			// {
			// }
			return true;
		}
		// LONGS
		if (iHandle.getInstruction() instanceof LSTORE)
		{
			LSTORE var = ((LSTORE) iHandle.getInstruction());
			String key = "L"+var.getIndex();
			Object val = ValueGetter.getPrevLong(iHandle.getPrev(), instList, cpgen);
			if (val != null)
				variables.put(key, (long) val);
			else if (variables.get(key) != null)
			{
				variables.remove(key);
			}
			return true;
		}
		// FLOATS 
		if (iHandle.getInstruction() instanceof FSTORE)
		{
			FSTORE var = ((FSTORE) iHandle.getInstruction());
			String key = "F"+var.getIndex();
			Object val = ValueGetter.getPrevFloat(iHandle.getPrev(), instList, cpgen);
			if (val != null)
				variables.put(key, (float) val);
			else if (variables.get(key) != null)
			{
				variables.remove(key);
			}
			return true;
		}
		// DOUBLES 
		if (iHandle.getInstruction() instanceof DSTORE)
		{
			DSTORE var = ((DSTORE) iHandle.getInstruction());
			String key = "D"+var.getIndex();
			Object val = ValueGetter.getPrevDouble(iHandle.getPrev(), instList, cpgen);
			if (val != null)
				variables.put(key, (double) val);
			else if (variables.get(key) != null)
			{
				variables.remove(key);
			}
			return true;
		}
		return false;
	}

	private void handleVariableLoads(InstructionHandle iHandle, Hashtable<String, Object> variables, InstructionList instList, ConstantPoolGen cpgen)
	{
		if (iHandle.getInstruction() instanceof ILOAD)
		{
			ILOAD var = (ILOAD) iHandle.getInstruction();
			String key = "I"+var.getIndex();
			Object val = variables.get(key);
			if (val != null)
				{
					System.out.println("...............optimising constant folding for integers");
					instList.insert(iHandle, new PUSH(cpgen, (int) val));
					try {
						instList.delete(iHandle);
					} catch (TargetLostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}
	}

	// ------------------------------------------------


	public void printInstructions(ClassGen classGen,ConstantPoolGen constPoolGen){
		getNumberConstant(constPoolGen);
		Method[] methods = classGen.getMethods();
		for (Method method : methods) {
			MethodGen methodGen = new MethodGen(method, classGen.getClassName(), constPoolGen);
			System.out.println(classGen.getClassName() + " > " + method.getName());
			System.out.println(methodGen.getInstructionList());
		}
	}

	public void getNumberConstant(ConstantPoolGen cpgen){
		ConstantPool cp = cpgen.getConstantPool();
		// get the constants in the pool
		Constant[] constants = cp.getConstantPool();
		for (int i = 0; i < constants.length; i++)
		{
			if (constants[i] instanceof ConstantInteger || constants[i] instanceof ConstantDouble || constants[i] instanceof ConstantLong || constants[i] instanceof ConstantFloat)
			{
				System.out.printf("%d) ",i);
				System.out.println(constants[i]);
			}
		}
		System.out.println();
	}
	
	public void getNumberConstantsPool(ConstantPool cp){
		// get the constants in the pool
		Constant[] constants = cp.getConstantPool();
		for (int i = 0; i < constants.length; i++)
		{
			if (constants[i] instanceof ConstantInteger || constants[i] instanceof ConstantDouble || constants[i] instanceof ConstantLong || constants[i] instanceof ConstantFloat)
			{
				System.out.printf("%d) ",i);
				System.out.println(constants[i]);
			}
		}
		System.out.println();
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
		/*
		Operators thatOperators that add nothing to the stack (e.g. NOP) are removed or replaced where needed add nothing to the stack  NOP are removed 
		*/
		methodGen.removeNOPs();
        System.out.println("**********************************");
        System.out.println("******Instructions before: *******");
        System.out.println("**************************Count: "+instList.getLength()); 
		System.out.println(instList.toString());
		//<name of opcode> "["<opcode number>"]" "("<length of instruction>")"
		// InstructionHandle is a wrapper for actual Instructions

		// OPTIMISE VARIABLES
		optimiseVariables(newMethod, instList, cgen, cpgen);
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
		instList = new InstructionList(newMethod.getCode().getCode());

		// for (InstructionHandle handle : instList.getInstructionHandles()) {
		// 	optimizeArithmetic(handle, instList, cgen, cpgen);	            
		// 	optimizeComparisons(handle, instList, cgen, cpgen);
			
		// 	// set max stack/local
		// 	methodGen.setMaxStack();
		// 	methodGen.setMaxLocals();
				
		// 	methodGen.setInstructionList(instList);
				
		// 	// remove local variable table
		// 	methodGen.removeLocalVariables();
	
		// 	// generate the new method with replaced instList
		// 	newMethod = methodGen.getMethod();
		// 	// replace the method in the original class
		// 	cgen.replaceMethod(method, newMethod);
		// }
		System.out.println("**********************************");
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
		// System.out.println("Before: \n\n");
		// printInstructions(cgen, cpgen);
        Method[] methods = cgen.getMethods();
		for (Method m : methods) {
			System.out.println("...........optimizing method: " + m.getName());
			optimizeMethod(cgen, cpgen, m);
		}
		// System.out.println("After: \n\n");
		// printInstructions(cgen, cpgen);

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