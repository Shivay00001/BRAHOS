package com.brahos.app.util

import com.brahos.app.domain.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class SafetyGuardrailTest {

    @Test
    fun `Normal symptoms do not trigger escalation`() {
        val symptoms = "Mild cough and cold"
        val level = SafetyGuardrail.getSafeRiskLevel(RiskLevel.GREEN_STABLE, symptoms, 25, 37.0f)
        assertEquals(RiskLevel.GREEN_STABLE, level)
    }

    @Test
    fun `Emergency keywords force RED risk level`() {
        val symptoms = "Patient complaining of severe chest pain"
        // AI incorrectly thinks it's stable
        val level = SafetyGuardrail.getSafeRiskLevel(RiskLevel.GREEN_STABLE, symptoms, 45, 37.0f)
        assertEquals(RiskLevel.RED_EMERGENCY, level)
    }

    @Test
    fun `Infant high fever forces RED risk level`() {
        val symptoms = "Baby crying constantly"
        // Case: 6 month old, 39C fever
        val level = SafetyGuardrail.getSafeRiskLevel(RiskLevel.YELLOW_OBSERVE, symptoms, 0, 39.5f)
        assertEquals(RiskLevel.RED_EMERGENCY, level)
    }

    @Test
    fun `Hypothermia forces RED risk level`() {
        val symptoms = "Skin is very cold"
        val level = SafetyGuardrail.getSafeRiskLevel(RiskLevel.GREEN_STABLE, symptoms, 30, 34.5f)
        assertEquals(RiskLevel.RED_EMERGENCY, level)
    }

    @Test
    fun `Safety rule cannot downgrade an AI-detected RED level`() {
        val symptoms = "Normal cough"
        // If AI already flagged it as RED (maybe it saw something else), stay RED
        val level = SafetyGuardrail.getSafeRiskLevel(RiskLevel.RED_EMERGENCY, symptoms, 30, 37.0f)
        assertEquals(RiskLevel.RED_EMERGENCY, level)
    }
}
