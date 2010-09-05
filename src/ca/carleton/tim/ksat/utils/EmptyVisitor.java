/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.carleton.tim.ksat.utils;

import org.eclipse.persistence.internal.libraries.asm.Attribute;
import org.eclipse.persistence.internal.libraries.asm.ClassVisitor;
import org.eclipse.persistence.internal.libraries.asm.CodeVisitor;
import org.eclipse.persistence.internal.libraries.asm.Label;

/**
 * An empty implementation of the ASM visitor interfaces.
 * 
 * @author Eric Bruneton
 * 
 * Note: the EclipseLink 'internalized' version of ASM (circa ~ version 1.5) 
 *       doesn't include the org.objectweb.asm.commons package. So, I
 *       grabbed a copy into KSAT's util package - mwnorman
 */
public class EmptyVisitor implements ClassVisitor, CodeVisitor {

	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
	}

	public void visitSource(final String source, final String debug) {
	}

	public void visitOuterClass(final String owner, final String name,
			final String desc) {
	}

	public void visitAttribute(final Attribute attr) {
	}

	public void visitInnerClass(final String name, final String outerName,
			final String innerName, final int access) {
	}

	public void visitEnd() {
	}

	public void visitCode() {
	}

	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
	}

	public void visitInsn(final int opcode) {
	}

	public void visitIntInsn(final int opcode, final int operand) {
	}

	public void visitVarInsn(final int opcode, final int var) {
	}

	public void visitTypeInsn(final int opcode, final String type) {
	}

	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
	}

	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
	}

	public void visitJumpInsn(final int opcode, final Label label) {
	}

	public void visitLabel(final Label label) {
	}

	public void visitLdcInsn(final Object cst) {
	}

	public void visitIincInsn(final int var, final int increment) {
	}

	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label labels[]) {
	}

	public void visitLookupSwitchInsn(final Label dflt, final int keys[],
			final Label labels[]) {
	}

	public void visitMultiANewArrayInsn(final String desc, final int dims) {
	}

	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
	}

	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
	}

	public void visitLineNumber(final int line, final Label start) {
	}

	public void visitMaxs(final int maxStack, final int maxLocals) {
	}

	public void visit(final String name, final Object value) {
	}

	public void visitEnum(final String name, final String desc,
			final String value) {
	}

	public void visit(int version, int access, String name, String superName,
			String[] interfaces, String sourceFile) {
	}

	public void visitField(int access, String name, String desc, Object value,
			Attribute attrs) {
	}

	public CodeVisitor visitMethod(int access, String name, String desc,
			String[] exceptions, Attribute attrs) {
		return this;
	}

	public void visitLocalVariable(String name, String desc, Label start,
			Label end, int index) {
	}

}