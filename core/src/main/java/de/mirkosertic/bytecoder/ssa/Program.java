/*
 * Copyright 2017 Mirko Sertic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mirkosertic.bytecoder.ssa;

import de.mirkosertic.bytecoder.core.BytecodeObjectTypeRef;
import de.mirkosertic.bytecoder.core.BytecodeOpcodeAddress;
import de.mirkosertic.bytecoder.core.BytecodeTypeRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Program extends SimpleControlFlowBlock {

    private final Map<Integer, Variable> variables;
    private int varCounter;

    public Program() {
        variables = new HashMap<>();
        varCounter = 0;
    }

    public Block createAt(BytecodeOpcodeAddress aAddress, Block.BlockType aType) {
        Block theNewBlock = new Block(aType, this, aAddress);
        add(theNewBlock);
        return theNewBlock;
    }

    public List<Variable> getVariables() {
        return new ArrayList<>(variables.values());
    }

    public Variable createVariable(Type aType, Value aValue) {
        int theIndex = varCounter++;
        Variable theNewVariable = new Variable(aType, "var" + theIndex, aValue);
        variables.put(theIndex, theNewVariable);
        return theNewVariable;
    }

    @Override
    public Set<BytecodeObjectTypeRef> getStaticReferences() {
        Set<BytecodeObjectTypeRef> theResult = super.getStaticReferences();
        for (Variable theVariable : variables.values()) {
            Value theValue = theVariable.getValue();
            if (theValue instanceof GetStaticValue) {
                GetStaticValue theStaticValue = (GetStaticValue) theValue;
                theResult.add(BytecodeObjectTypeRef.fromUtf8Constant(theStaticValue.getField().getClassIndex().getClassConstant().getConstant()));
            }
            if (theValue instanceof ClassReferenceValue) {
                ClassReferenceValue theClassRef = (ClassReferenceValue) theValue;
                theResult.add(theClassRef.getType());
            }
            if (theValue instanceof NewArrayValue) {
                NewArrayValue theNewArray = (NewArrayValue) theValue;
                if (theNewArray.getType() instanceof BytecodeObjectTypeRef) {
                    theResult.add((BytecodeObjectTypeRef) theNewArray.getType());
                }
            }
            if (theValue instanceof NewMultiArrayValue) {
                NewMultiArrayValue theNewArray = (NewMultiArrayValue) theValue;
                BytecodeTypeRef theTypeRef = theNewArray.getType();
                if (theTypeRef instanceof BytecodeObjectTypeRef) {
                    theResult.add((BytecodeObjectTypeRef) theTypeRef);
                }
            }
            if (theValue instanceof NewObjectValue) {
                NewObjectValue theNewObjectValue = (NewObjectValue) theValue;
                theResult.add(BytecodeObjectTypeRef.fromUtf8Constant(theNewObjectValue.getType().getConstant()));
            }
        }
        return theResult;
    }

    public void removeVariable(Variable aVariable) {
        for (Map.Entry<Integer, Variable> theEntry : variables.entrySet()) {
            if (theEntry.getValue() == aVariable) {
                variables.remove(theEntry.getKey());
                return;
            }
        }
    }
}