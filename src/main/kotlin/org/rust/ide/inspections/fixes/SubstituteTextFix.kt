/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.inspections.fixes

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Fix that removes the given range from the document and places a text onto its place.
 * @param psi The element to substitute text in
 * @param rangeInElement The range *inside element* that will be removed from the document.
 * @param substitution The text that will be placed starting from `range.startOffset`. If `null`, no text will be inserted.
 * @param fixName The name to use for the fix instead of the default one to better fit the inspection.
 */
class SubstituteTextFix private constructor(
    psi: PsiElement,
    private val fixName: String = "Substitute",
    private val rangeInElement: TextRange,
    private val substitution: String?
) : LocalQuickFixOnPsiElement(psi) {
    init {
        check(psi.textRange.shiftRight(-psi.textRange.startOffset).contains(rangeInElement)) {
            "Please pass relative range inside the element to SubstituteTextFix"
        }
    }

    override fun getText(): String = fixName
    override fun getFamilyName() = "Substitute one text to another"

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val document = PsiDocumentManager.getInstance(project).getDocument(file)
        val range = rangeInElement.shiftRight(startElement.textRange.startOffset)
        document?.deleteString(range.startOffset, range.endOffset)
        if (substitution != null) {
            document?.insertString(range.startOffset, substitution)
        }
    }

    companion object {
        fun delete(fixName: String, psi: PsiElement, rangeInElement: TextRange) =
            SubstituteTextFix(psi, fixName, rangeInElement, null)

        fun insert(fixName: String, psi: PsiElement, offsetInElement: Int, text: String) =
            SubstituteTextFix(psi, fixName, TextRange(offsetInElement, offsetInElement), text)

        fun replace(fixName: String, psi: PsiElement, rangeInElement: TextRange, text: String) =
            SubstituteTextFix(psi, fixName, rangeInElement, text)
    }
}
