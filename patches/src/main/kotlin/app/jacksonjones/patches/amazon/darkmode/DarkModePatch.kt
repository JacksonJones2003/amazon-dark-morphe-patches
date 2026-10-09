package app.jacksonjones.patches.amazon.darkmode

import app.jacksonjones.patches.shared.Constants.COMPATIBILITY_AMAZON_SHOPPING
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val WEBVIEW_CLASS = "Landroid/webkit/WebView;"
private const val EXTENSION_PACKAGE = "Lapp/jacksonjones/extension/"
private const val EXTENSION_WEBVIEW_CLASS = "${EXTENSION_PACKAGE}amazon/DarkWebView;"

private fun Instruction.isWebViewNewInstance() =
    opcode == Opcode.NEW_INSTANCE &&
            ((this as ReferenceInstruction).reference as TypeReference).type == WEBVIEW_CLASS

private fun Instruction.isWebViewConstructorCall(): Boolean {
    if (opcode != Opcode.INVOKE_DIRECT && opcode != Opcode.INVOKE_DIRECT_RANGE) return false

    val reference = (this as ReferenceInstruction).reference as MethodReference
    return reference.definingClass == WEBVIEW_CLASS && reference.name == "<init>"
}

private fun ClassDef.usesWebView() = superclass == WEBVIEW_CLASS || methods.any { method ->
    method.implementation?.instructions?.any {
        it.isWebViewNewInstance() || it.isWebViewConstructorCall()
    } == true
}

@Suppress("unused")
val darkModePatch = bytecodePatch(
    name = "Dark mode",
    description = "Adds a dark mode that follows the system dark theme. " +
            "Web pages shown in the app are darkened, and optionally the native parts of the app.",
    default = true
) {
    compatibleWith(COMPATIBILITY_AMAZON_SHOPPING)

    val darkenNativeUi = booleanOption(
        key = "darkenNativeUi",
        default = true,
        title = "Darken native UI",
        description = "Let Android automatically darken the parts of the app that are not web pages, " +
                "such as the search bar and the bottom navigation bar. " +
                "Turn this off if these parts of the app look wrong."
    )

    dependsOn(darkModeResourcePatch { darkenNativeUi.value != false })

    extendWith("extensions/extension.mpe")

    execute {
        // Nearly all the app content is web pages shown in a WebView. The app creates them from
        // many different classes, so instead of fingerprinting each one, every WebView of the app
        // is changed to the extension WebView that turns on dark mode for itself.
        val classesUsingWebView = mutableListOf<ClassDef>()
        classDefForEach { classDef ->
            if (!classDef.type.startsWith(EXTENSION_PACKAGE) && classDef.usesWebView()) {
                classesUsingWebView += classDef
            }
        }

        if (classesUsingWebView.isEmpty()) {
            throw PatchException("Could not find any WebView usage")
        }

        classesUsingWebView.forEach { classDef ->
            val mutableClass = mutableClassDefBy(classDef)

            // Change classes that extend WebView to extend the extension WebView.
            if (mutableClass.superclass == WEBVIEW_CLASS) {
                mutableClass.setSuperClass(EXTENSION_WEBVIEW_CLASS)
            }

            mutableClass.methods.forEach { method ->
                val instructions = method.implementation?.instructions ?: return@forEach

                // Replacing an instruction does not change the index of other instructions.
                instructions.toList().forEachIndexed { index, instruction ->
                    if (instruction.isWebViewNewInstance()) {
                        val register = (instruction as OneRegisterInstruction).registerA

                        method.replaceInstruction(
                            index,
                            "new-instance v$register, $EXTENSION_WEBVIEW_CLASS"
                        )
                    } else if (instruction.isWebViewConstructorCall()) {
                        // Both the constructors called with "new WebView()",
                        // and the super constructor calls of classes that extend WebView.
                        val reference = (instruction as ReferenceInstruction).reference as MethodReference
                        val constructor = "$EXTENSION_WEBVIEW_CLASS-><init>(" +
                                "${reference.parameterTypes.joinToString("")})V"

                        val replacement = if (instruction.opcode == Opcode.INVOKE_DIRECT_RANGE) {
                            instruction as RegisterRangeInstruction
                            val first = instruction.startRegister
                            val last = first + instruction.registerCount - 1

                            "invoke-direct/range { v$first .. v$last }, $constructor"
                        } else {
                            instruction as FiveRegisterInstruction
                            val registers = listOf(
                                instruction.registerC,
                                instruction.registerD,
                                instruction.registerE,
                                instruction.registerF,
                                instruction.registerG,
                            ).take(instruction.registerCount).joinToString { "v$it" }

                            "invoke-direct { $registers }, $constructor"
                        }

                        method.replaceInstruction(index, replacement)
                    }
                }
            }
        }
    }
}
